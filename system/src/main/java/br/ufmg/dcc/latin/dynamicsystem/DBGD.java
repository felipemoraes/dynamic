package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.metrics.CubeTest;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;
import br.ufmg.dcc.latin.reranking.InteractiveRerankerFactory;

public class DBGD {
	
	private int n;
	private double alpha;
	private double delta;
	private int iterations;
	
	private String index;
	private String topic;
	private String query;
	
	private List<String[]> trainingSet;
	private List<String[]> validationSet;
	
	int nextIndex;
	
	InteractiveReranker reranker;
	
	private double lambda;
	
	public void setupReranker(String rerankerName){
		reranker = InteractiveRerankerFactory.getInstance(rerankerName, "FeaturedAspectMining");
	}
	
	public void loadTrainingSet(String trainingFilename) throws IOException{
		trainingSet = new ArrayList<String[]>();
		
		BufferedReader br = new BufferedReader(new FileReader(trainingFilename));
		String line;
		while ((line = br.readLine()) != null) {
		    String[] splitLine = line.split(" ",3);
		    trainingSet.add(splitLine);
		}
		br.close();
		Collections.shuffle(trainingSet);
		nextIndex = 0;
	}
	
	public void loadValidationSet(String validationFilename) throws IOException {
		validationSet = new ArrayList<String[]>();
		BufferedReader br = new BufferedReader(new FileReader(validationFilename));
		String line;
		while ((line = br.readLine()) != null) {
		    String[] splitLine = line.split(" ",3);
		    validationSet.add(splitLine);
		}
		br.close();
	}
	
	private void nextQuery(){
		
		if (nextIndex == trainingSet.size()){
			nextIndex = 0;
			Collections.shuffle(trainingSet);
		}
		
		String[] queryInfo = trainingSet.get(nextIndex);
		this.index = queryInfo[0];
		this.topic = queryInfo[1];
		this.query = queryInfo[2];
		nextIndex++;
	}
	
	
	
	public double[] train(){
		
		double[] w0 = initVector();
		double[] w1 = initVector();

		CubeTest cubeTest = new CubeTest();
		
		System.out.println("Starting training ...");
		
		for (int i = 0; i < iterations*trainingSet.size(); i++) {
			
			if (i % 100 == 0) {
				System.out.println(i + " of " + iterations*trainingSet.size());
			}
			
			nextQuery();
			
			RetrievalCache.topicId = topic;
			RetrievalCache.indexName = index;
			reranker.start(query, index);
			reranker.start(w0);
			
			String[][] resultsSoFar = new String[10][5];
			for (int j = 0; j < n; j++) {
				
				double[] disturb = getDisturb(8);
				for (int k = 2; k < resultsSoFar.length; k++) {
					w1[k] = w0[k] + (delta*disturb[k-2]);
				}
				
				reranker.setParams(w0);
				ResultSet resultSet0 = reranker.get();
				resultsSoFar[j] = resultSet0.docnos;
				double metric0 = cubeTest.getAverageCubeTest(j+1, topic, resultsSoFar);
				reranker.setParams(w1);
				ResultSet resultSet1 = reranker.get();

				resultsSoFar[j] = resultSet1.docnos;
				for (int k = 0; k < 5; k++) {
					resultsSoFar[j][k] = resultSet1.docnos[k];
				}
				double metric1 = cubeTest.getAverageCubeTest(j+1, topic, resultsSoFar);
				double d = metric1 - metric0;
				if (d>0) {
					System.out.println(metric1 + "  " + metric0);
					for (int k = 0; k < resultSet0.docnos.length; k++) {
						System.out.println(resultSet0.docnos[k] + " " + resultSet1.docnos[k]) ;
					}
					for (int k = 2; k < resultsSoFar.length; k++) {
						w0[k] = w0[k] + (alpha*disturb[k-2]);
						
						System.out.print(w0[k] + " ");
						
					}
					System.out.println();
					reranker.setParams(w0);
					resultSet0 = reranker.get();
					for (int k = 0; k < 5; k++) {
						resultsSoFar[j][k] = resultSet0.docnos[k];
					}
					Feedback[] feedback = TrecUser.get(resultSet0, topic);
					reranker.update(feedback);
				} else {
					Feedback[] feedback = TrecUser.get(resultSet0, topic);
					reranker.update(feedback);
				}
			}
		}
			
		return w0;
		
	}
	
	public double validate(double[] weight){
		double totalMetric = 0;
		CubeTest cubeTest = new CubeTest();
		for (String[] queryInfo: validationSet) {
			this.index = queryInfo[0];
			this.topic = queryInfo[1];
			this.query = queryInfo[2];
			String[][] result = new String[n][5];
			RetrievalCache.topicId = topic;
			RetrievalCache.indexName = index;
			reranker.start(query, index);
			reranker.start(weight);
			for (int i = 0; i < n; i++) {
				ResultSet resultSet = reranker.get();
				result[i] = resultSet.docnos;
				Feedback[] feedback = TrecUser.get(resultSet, topic);
				reranker.update(feedback);
			}
			totalMetric += cubeTest.getAverageCubeTest(n, topic, result);
		}
		if (totalMetric > 0) {
			totalMetric /= validationSet.size();
		}
		return totalMetric;
	}
	
	public double[] initVector(){
		double[] weights = new double[10];
		weights[0] = 1000d;
		weights[1] = lambda;
		double[] rand = getDisturb(8);
		for (int i = 2; i < weights.length; i++) {
			weights[i] = rand[i-2];
		}
		return weights;
	}
	private static Random randGenerator = new Random();
	
	private static double[] getDisturb(int numberOfFeatures) {
		double[] disturb = new double[numberOfFeatures];
		for (int i = 0; i < disturb.length; i++) {
			disturb[i] = (randGenerator.nextDouble() * 2.0) -1.0;
		}
		return normalizeL2(disturb);
	}
	
	
	
	public static double[] normalizeL2(double[] vector) {
		// compute vector 2-norm
		double norm2 = 0.0;
		for (int i = 0; i < vector.length; i++) {
			norm2 += vector[i] * vector[i];
		}
		norm2 = (double) Math.sqrt(norm2);

		if (norm2 == 0) {
			Arrays.fill(vector, 1);
		} else {
			for (int i = 0; i < vector.length; i++) {
				vector[i] = vector[i] / norm2;
			}
		}
		return vector;
	}

	public void setParam(double[] param) {
		alpha = param[0];
		delta = param[1];
		iterations = (int) param[2];
		lambda = param[3];
		n = 10;
	}
	
}
