package br.ufmg.dcc.latin.models;


import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.ufmg.dcc.latin.grpc.learner.Document;
import br.ufmg.dcc.latin.grpc.learner.Label;
import br.ufmg.dcc.latin.grpc.learner.LearnerGrpc;
import br.ufmg.dcc.latin.grpc.learner.LoadReply;
import br.ufmg.dcc.latin.grpc.learner.LoadRequest;
import br.ufmg.dcc.latin.grpc.learner.SimilarityRequest;
import br.ufmg.dcc.latin.grpc.learner.SimilarityResponse;
import br.ufmg.dcc.latin.search.elements.Concept;
import br.ufmg.dcc.latin.search.elements.Feedback;
import br.ufmg.dcc.latin.search.elements.Subtopic;
import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.SearchResource;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class xQuADiParameterBuilder {
	
	private static SearchResource searchResource;
	
	private static String host = "localhost";
	private static int port = 50051;
	private static ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port).usePlaintext(true)
	        .build();
	private static LearnerGrpc.LearnerBlockingStub stub = LearnerGrpc.newBlockingStub(channel);
	
	public static xQuADiParameter build(String index, String model, String query){
		if (searchResource == null) {
			searchResource = new SearchResource(index,"doc");
		} else if (!searchResource.getIndexName().equals(index)){
			searchResource.setIndexName(index);
		}
		String[] fields = {"text"};
		
		ResultSet resultSet = searchResource.search(query,fields, 1000);
		xQuADiParameter param = new xQuADiParameter();
		param.docIds = resultSet.getDocIds();
		param.docNos = resultSet.getDocNos();
		param.docContent = resultSet.getDocContent();
		param.depth = 5;
		param.k = 0;
		param.ambiguity = 0.3	;
		int n = param.docNos.length;
		ArrayList<Document> documents = new ArrayList<Document>();
		for (int i = 0; i < param.docContent.length; i++) {
			documents.add(Document.newBuilder().setId(i).setContent(param.docContent[i]).build());
		}
		
		LoadRequest loadRequest = LoadRequest.newBuilder().addAllDocuments(documents).build();
		LoadReply reply = stub.load(loadRequest);
		
		
		param.coverage = new double[n][100];
		param.importance = new double[100];
		
		param.relevance = new double[n];
		for(int i = 0 ; i < n; ++i){
			param.relevance[i] = (double) resultSet.getScores()[i];
		}
		
		
		
		return param;
		
	}
	
	public static Concept[] createSubtopicConcepts(ArrayList<Subtopic> subtopics){
		String allPassagens =  "";
		for (Subtopic subtopic : subtopics) {
			allPassagens += subtopic.getPassageText() + " ";
		}
		
		return createConceptsFromText(allPassagens);
	}
	
	
	public static void createDocConcepts(xQuADiParameter param){
		int n = param.docIds.length;
		//docConcepts = new Concept[n][];
		for (int i = 0; i < param.docContent.length; ++i) {
			//docConcepts[i] = createConceptsFromText(param.docContent[i]);
		}
	}
	
	private static Concept[] createConceptsFromText(String docContent){
		
		
		Map<String,Integer> accumulator = new HashMap<String,Integer>();
		
		TokenizerFactory<Word> tf = PTBTokenizer.factory();
		if (docContent == null){
			docContent = "";
		}
		List<Word> tokens_words = tf.getTokenizer(new StringReader(docContent)).tokenize();
		for (Word word : tokens_words) {
			int freq = accumulator.getOrDefault(word.word(), 0);
			accumulator.put(word.word(), freq+1);
		}
		Concept[] concepts = new Concept[accumulator.size()];
		int i = 0;
		for (Entry<String,Integer> pair : accumulator.entrySet()) {
			
			concepts[i] = new Concept();
			concepts[i].conceptId = pair.getKey();
			concepts[i].weight = pair.getValue();
			i++;
		}
		
		return concepts;
	}
	
	
	private static double conceptScores(Concept[] subtopicConcepts, Concept[] docConcepts){
		Map<String,Double> subtopic = new HashMap<String,Double>();
		Map<String,Double> doc = new HashMap<String,Double>();
		
		double score = 0.0F;
		
		for(int i = 0; i < subtopicConcepts.length; ++i){
		
			subtopic.put(subtopicConcepts[i].conceptId, subtopicConcepts[i].weight);
		}
		
		for(int i = 0; i < docConcepts.length; ++i){
			doc.put(docConcepts[i].conceptId, docConcepts[i].weight);
		}
		
		for (Entry<String,Double> concept : subtopic.entrySet()) {
			if (doc.containsKey(concept.getKey())) {
				
				score += concept.getValue()*doc.get(concept.getKey());
			}
		}
		if (subtopicConcepts.length*docConcepts.length > 0) {
			score /=  subtopicConcepts.length*docConcepts.length;
		}
		
		return score;
	}
	
	public static xQuADiParameter rebuildWithFeedback(xQuADiParameter param , Feedback[] feedback){
		
		ArrayList<Label> labels = new ArrayList<Label>();
		for (int i = 0; i < feedback.length; i++) {
			if (feedback[i] == null) {
				continue;
			}
			if (!feedback[i].getOnTopic()){
				continue;
			}
			for (Subtopic subtopic : feedback[i].getSubtopics()) {
				labels.add(Label.newBuilder().addLabels(subtopic.getId()).addContent(subtopic.getPassageText()).build());
			}
		}
		SimilarityRequest request = SimilarityRequest.newBuilder().addAllLabels(labels).build();
		SimilarityResponse response = stub.getSimilarities(request);
		System.out.println(response.getDataList());
		Map<String,ArrayList<Subtopic>> subtopicsPassagens = new HashMap<String,ArrayList<Subtopic>>();
		
		for (int i = 0; i < feedback.length; i++) {
			if (feedback[i] == null) {
				continue;
			}
			if (!feedback[i].getOnTopic()){
				continue;
			}
			Subtopic[] subtopics = feedback[i].getSubtopics();
			if (subtopics == null) {
				continue;
			}
			for (int j = 0; j < subtopics.length; ++j) {
				if (!subtopicsPassagens.containsKey(subtopics[j].getId())){
					subtopicsPassagens.put(subtopics[j].getId(), new ArrayList<Subtopic>());
				}
				subtopicsPassagens.get(subtopics[j].getId()).add(subtopics[j]);
			}
		}
		
		int k = subtopicsPassagens.size();
		int n = param.docIds.length;
		
		
		
		//Create subtopicConcept
		int totalSubtopicsLength = 0;
		Concept[][] subtopicConcepts = new Concept[k][];
		int m = 0;
		for (Entry<String,ArrayList<Subtopic>> subtopic : subtopicsPassagens.entrySet()) {
			totalSubtopicsLength += subtopic.getValue().size();
			subtopicConcepts[m] = createSubtopicConcepts(subtopic.getValue());
			param.importance[m] = subtopic.getValue().size();
			m++;
		}

		param.k = k;
		//Update coverage matrix and importance
		for (int i = 0; i < k; i++) {
			//param.importance[i] = (double) param.importance[i]/totalSubtopicsLength;
			param.importance[i] = 1;
			for (int j = 0; j < n; j++) {
				//param.coverage[j][i] = conceptScores(subtopicConcepts[i], docConcepts[j]);
				//System.out.print(param.coverage[j][i] + " ");
			}
			//System.out.println();
			
		}
		//System.out.println();
		
		
		
		return param;
	}
}
