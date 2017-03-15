package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;
import br.ufmg.dcc.latin.reranking.InteractiveRerankerFactory;

public class TestStatic {
	private static List<String[]> testSet;
	private static double[] bestParam = null;
	private static double[] bestWeights = null;
	private static InteractiveReranker reranker;
	
	public static void loadTestFile(String testFilename) throws IOException {
		testSet = new ArrayList<String[]>();
		BufferedReader br = new BufferedReader(new FileReader(testFilename));
		String line;
		while ((line = br.readLine()) != null) {
		    String[] splitLine = line.split(" ",3);
		    testSet.add(splitLine);
		}
		br.close();
	}
	
	public static void loadModel(String testFilename) throws IOException {

		
		BufferedReader br = new BufferedReader(new FileReader(testFilename));
		String line = br.readLine();
		
		String[] splitLine = line.split(" ");
		bestParam = new double[splitLine.length+1];
		for (int i = 0; i < splitLine.length; i++) {
			bestParam[i+1] = Double.parseDouble(splitLine[i]);
		}
		bestParam[0] = 1000d;

		bestWeights = new double[bestParam.length+1];
		
	
		for (int i = 0; i < bestParam.length; i++) {
			bestWeights[i] = bestParam[i];
		}
		
		
		br.close();
	}

	public static void setupReranker(String rerankerName){
		reranker = InteractiveRerankerFactory.getInstance(rerankerName, "PassageAspectMining");
	}
	
	public static void main(String[] args) throws IOException {
		
		String testFilename = args[0];
		String modelFilename = args[1];
		String runname = args[2];
		String rerankerName = args[3];
		
		TrecUser.load("../share/truth_data.txt");
		
		loadTestFile(testFilename);
		loadModel(modelFilename);
		setupReranker(rerankerName);
		
		for (int i = 0; i < testSet.size(); i++) {
			String index = testSet.get(i)[0];
			String topic = testSet.get(i)[1];
			String query = testSet.get(i)[2];
			
			reranker.start(query, index);
			reranker.start(bestWeights);
			System.out.println(bestWeights[0] + " " + bestWeights[1]);
			RetrievalCache.topicId = topic;
			RetrievalCache.indexName = index;
			System.out.println(topic);
			for (int j = 0; j < 10; j++) {
				ResultSet resultSet = reranker.get();
				Evaluator.writeToFile(runname, topic, resultSet, j);
				Feedback[] feedback = TrecUser.get(resultSet, topic);
				reranker.update(feedback);
			}
			
		}


	}
}
