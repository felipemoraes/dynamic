package br.dcc.latin.searcher;

import static org.junit.Assert.*;

import java.util.ArrayList;

import org.junit.Test;

import br.ufmg.dcc.latin.grpc.learner.Document;
import br.ufmg.dcc.latin.grpc.learner.Label;
import br.ufmg.dcc.latin.grpc.learner.LearnerGrpc;
import br.ufmg.dcc.latin.grpc.learner.LoadReply;
import br.ufmg.dcc.latin.grpc.learner.LoadRequest;
import br.ufmg.dcc.latin.grpc.learner.SimilarityRequest;
import br.ufmg.dcc.latin.grpc.learner.SimilarityResponse;
import br.ufmg.dcc.latin.models.xQuADiParameter;
import br.ufmg.dcc.latin.models.xQuADiParameterBuilder;
import br.ufmg.dcc.latin.search.elements.Feedback;
import br.ufmg.dcc.latin.search.elements.Subtopic;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class xQuADiParameterBuilderTest {

	@Test
	public void test() {
		xQuADiParameter param = new xQuADiParameter();
		param.docIds = new int[2];
		param.relevance = new double[2];
		param.docContent = new String[2];
		param.docIds[0] = 1;
		param.docIds[1] = 2;
		param.relevance[0] = 0.8;
		param.relevance[1] = 0.5;
		param.docContent[0] = "james bond film film film";
		param.docContent[1] = "james bond book book book";
		param.ambiguity = 0.5;
		param.depth = 1;
		String host = "localhost";
		int port = 50051;
		ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true)
		        .build();
		
		LearnerGrpc.LearnerBlockingStub blockingStub = LearnerGrpc.newBlockingStub(channel);
		ArrayList<Document> documents = new ArrayList<Document>();
		documents.add(Document.newBuilder().setId(1).setContent("james bond film film film").build());
		documents.add(Document.newBuilder().setId(2).setContent("james bond book book book").build());
		LoadRequest loadRequest = LoadRequest.newBuilder().addAllDocuments(documents).build();
		LoadReply reply = blockingStub.load(loadRequest);
		ArrayList<String> labels = new ArrayList<String>();
		labels.add("bond filme");
		SimilarityRequest request = SimilarityRequest.newBuilder()
				.addLabels(Label.newBuilder().addLabels("S1").addContent("bond film"))
				.addLabels(Label.newBuilder().addLabels("S1").addContent("james film"))
				.addLabels(Label.newBuilder().addLabels("S2").addContent("bond book"))
				.addLabels(Label.newBuilder().addLabels("S2").addContent("bond book"))
				.build();
		
		SimilarityResponse response = blockingStub.getSimilarities(request);
	
		System.out.println(response.getDataList());
		Feedback[] feedback = new Feedback[2];
		Subtopic[] subtopics1 = new Subtopic[2];
		subtopics1[0] = new Subtopic("1",5,"bond film");
		subtopics1[1] = new Subtopic("1",5,"james film");
		feedback[0] = new Feedback();
		feedback[0].setSubtopics(subtopics1);
		
		Subtopic[] subtopics2 = new Subtopic[2];
		subtopics2[0] = new Subtopic("2",5,"bond book");
		subtopics2[1] = new Subtopic("2",5,"bond book");

		feedback[1] = new Feedback();
		feedback[1].setSubtopics(subtopics2);
		xQuADiParameterBuilder.createDocConcepts(param);
		param.coverage = new double[100][2];
		param.importance = new double[100];
		param = xQuADiParameterBuilder.rebuildWithFeedback(param, feedback);
		
		System.out.println(param.coverage[0][0]);
		System.out.println(param.coverage[1][0]);
		System.out.println(param.coverage[0][1]);
		System.out.println(param.coverage[1][1]);
		System.out.println(param.importance[0]);
		System.out.println(param.importance[1]);
	}

}
