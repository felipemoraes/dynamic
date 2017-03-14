package br.ufmg.dcc.latin.learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.dynamicsystem.TrecUser;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.metrics.CubeTest;
import br.ufmg.dcc.latin.metrics.nDCG;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;
import br.ufmg.dcc.latin.reranking.InteractiveRerankerFactory;

public class DBGD implements Learner {
	
	private int n;
	private double alpha;
	private double delta;
	private int iterations;
	
	private String index;
	private String topic;
	private String query;
	
	private List<String[]> trainingSet;
	private List<String[]> validationSet;
	
	double[] w0;
	double[] bestW0;
	CubeTest cubeTest;
	nDCG ndcg;
	int nextIndex;
	double currentMetric;
	
	InteractiveReranker reranker;
	
	private double lambda;
	
	public void setupReranker(String rerankerName){
		reranker = InteractiveRerankerFactory.getInstance(rerankerName, "FeaturedAspectMining");
	}
	
	public void loadTrainingSet(String trainingFilename){
		trainingSet = new ArrayList<String[]>();
	
		try {
			BufferedReader br = new BufferedReader(new FileReader(trainingFilename));
			String line;
			while ((line = br.readLine()) != null) {
			    String[] splitLine = line.split(" ",3);
			    trainingSet.add(splitLine);
			}
			br.close();
			Collections.shuffle(trainingSet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		nextIndex = 0;
	}
	
	public void loadValidationSet(String validationFilename) {
		validationSet = new ArrayList<String[]>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(validationFilename));
			String line;
			while ((line = br.readLine()) != null) {
			    String[] splitLine = line.split(" ",3);
			    validationSet.add(splitLine);
			}
			br.close();
		} catch (IOException e) {
				e.printStackTrace();
		}
	}
	
	public void nextQuery(){
		
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
		
		w0 = initVector();
		bestW0 = w0;
		currentMetric = 0;
		double[] w1 = initVector();

		cubeTest = new CubeTest();
		ndcg = new nDCG();
		
		System.out.println("Starting training ...");
		
		for (int i = 0; i < iterations; i++) {
			
			if (i % 100 == 0) {
				System.out.println(i + " of " + iterations);
			}
			
			double metric0 = 0;
			for (int k = 0; k < trainingSet.size(); k++) {
				nextQuery();

				RetrievalCache.topicId = topic;
				RetrievalCache.indexName = index;
				reranker.start(query, index);
				reranker.start(w0);	
				String[][] resultsSoFar = new String[10][5];
				for (int j = 0; j < n; j++) {
					ResultSet resultSet = reranker.get();
					resultsSoFar[j] = resultSet.docnos;
					Feedback[] feedback = TrecUser.get(resultSet, topic);
					reranker.update(feedback);
				}
				metric0 += ndcg.getNDCG(n, topic, resultsSoFar);
			}
			
			metric0 = metric0/trainingSet.size();
			
			
			double metric1 = 0;
			
			double[] disturb = getDisturb(6);
			for (int k = 0; k < disturb.length; k++) {
				w1[k+2] = w0[k+2] + (delta*disturb[k]);
				System.out.print(w1[k+2] + " ");
			}
			System.out.println();
			
			for (int k = 0; k < trainingSet.size(); k++) {
				nextQuery();
				RetrievalCache.topicId = topic;
				RetrievalCache.indexName = index;
				reranker.start(query, index);
				reranker.start(w1);	
				String[][] resultsSoFar = new String[10][5];
				for (int j = 0; j < n; j++) {
					ResultSet resultSet = reranker.get();
					resultsSoFar[j] = resultSet.docnos;
					Feedback[] feedback = TrecUser.get(resultSet, topic);
					reranker.update(feedback);
				}
				metric1 += ndcg.getNDCG(n, topic, resultsSoFar);
			}
			
			metric1 = metric1/trainingSet.size();
			

			double d = metric1 - metric0;

			if (d>0.00) {
				
				for (int k = 0; k < disturb.length; k++) {
					w0[k+2] = w0[k+2] + (alpha*disturb[k]);
				//	System.out.print(w0[k+2] + " ");
				}
				double metric = 0;
				for (int k = 0; k < trainingSet.size(); k++) {
					nextQuery();
					RetrievalCache.topicId = topic;
					RetrievalCache.indexName = index;
					reranker.start(query, index);
					reranker.start(w0);	
					String[][] resultsSoFar = new String[10][5];
					for (int j = 0; j < n; j++) {
						ResultSet resultSet = reranker.get();
						resultsSoFar[j] = resultSet.docnos;
						Feedback[] feedback = TrecUser.get(resultSet, topic);
						reranker.update(feedback);
					}
					metric += ndcg.getNDCG(n, topic, resultsSoFar);
				}
				
				System.out.println(metric/trainingSet.size());
			}  else {
				System.out.println(metric0);
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
			// System.out.println(topic + " " + cubeTest.getAverageCubeTest(n, topic, result));
			//totalMetric += cubeTest.getAverageCubeTest(n, topic, result);
			totalMetric += ndcg.getNDCG(n, topic, result);
		}
		if (totalMetric > 0) {
			totalMetric /= validationSet.size();
		}
		return totalMetric;
	}
	
	public double[] initVector(){
		double[] weights = new double[8];
		weights[0] = 1000d;
		weights[1] = lambda;
		double[] rand = getDisturb(6);
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
	
	public List<double[]> getParams(){
		List<Double> alphas = new ArrayList<Double>();
		List<Double> deltas = new ArrayList<Double>();
		List<Double> iterations = new ArrayList<Double>();
		List<Double> lambdas = new ArrayList<Double>();
		double step = 0.2;
		//for (int i = 0; i < 5; i++) {
		//	alphas.add(step*(i+1));
		//	deltas.add(step*(i+1));
		//}
		alphas.add(0.1d);
		
		deltas.add(0.1d);
		step = 0.1;
		for (int i = 0; i < 10	; i++) {
			lambdas.add(step*(i+1));
		}
		
		iterations.add(1000d);

		List<List<Double>> lists = new ArrayList<List<Double>>();
		lists.add(alphas);
		lists.add(deltas);
		lists.add(iterations);
		lists.add(lambdas);
		List<List<Double>> product = cartesianProduct(lists);
		List<double[]> experimentalParameters = new ArrayList<double[]>();
		for (List<Double> list : product) {
			int n = list.size();
			double[] params = new double[n];
			for (int i = 0; i < n; i++) {
				params[i] = list.get(i);
			}
			experimentalParameters.add(params);
		}
		return experimentalParameters;
	}
	
	public void dumpModel(String modelFile, double[] bestParam, double[] bestWeights) {
		try(FileWriter fw = new FileWriter(modelFile);
				BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {
			for (int i = 0; i < bestParam.length; i++) {
				out.write(bestParam[i] + " ");
			}
			out.write("\n");
			
			for (int i = 2; i < bestWeights.length; i++) {
				out.write(bestWeights[i] + " ");
			}
			out.write("\n");
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
}