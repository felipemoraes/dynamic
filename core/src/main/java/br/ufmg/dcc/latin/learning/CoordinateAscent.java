package br.ufmg.dcc.latin.learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.dynamicsystem.TrecUser;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.metrics.CubeTest;
import br.ufmg.dcc.latin.metrics.nDCG;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;
import br.ufmg.dcc.latin.reranking.InteractiveRerankerFactory;

public class CoordinateAscent implements Learner {

	
	private String index;
	private String topic;
	private String query;
	
	private List<String[]> trainingSet;
	private List<String[]> validationSet;
	
	int nextIndex;
	double lambda;
	nDCG ndcg;
	CubeTest cubeTest;
	
	InteractiveReranker reranker;
	
	@Override
	public void setupReranker(String rerankerName) {
		reranker = InteractiveRerankerFactory.getInstance(rerankerName, "FeaturedAspectMining");
	}

	@Override
	public void loadTrainingSet(String trainingFilename) {
		trainingSet = new ArrayList<String[]>();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(trainingFilename));
			String line;
			while ((line = br.readLine()) != null) {
			    String[] splitLine = line.split(" ",3);
			    trainingSet.add(splitLine);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		nextIndex = 0;
		
	}

	@Override
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

	@Override
	public void nextQuery() {
		if (nextIndex == trainingSet.size()){
			nextIndex = 0;
		}
		
		String[] queryInfo = trainingSet.get(nextIndex);
		this.index = queryInfo[0];
		this.topic = queryInfo[1];
		this.query = queryInfo[2];
		nextIndex++;
	}
	
	public double[] initVector(){
		double[] weights = new double[11];
		weights[0] = 1000d;
		weights[1] = lambda;
		for (int i = 2; i < weights.length; i++) {
			weights[i] = 0.5;
		}
		return weights;
	}

	@Override
	public double[] train() {
		
		cubeTest = new CubeTest();
		double[] currentW = initVector();
		List<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i < 9; i++) {
			indices.add(i);
		}
		Collections.shuffle(indices);
		double currentScore = evaluate(currentW);
		while (true) {
			double[] newW = currentW;
			for (int i = 0; i < indices.size(); i++) {
				int d = indices.get(i);
				System.out.println("Picked " + d);
				newW = findMaxW(d,newW);
				System.out.print("Best W: ");
				for (int j = 2; j < newW.length; j++) {
					System.out.print(newW[j] + " ");
				}
				System.out.println();
			}
			double score = evaluate(newW);
			System.out.println("Score found: " + score + " Current Score:" + currentScore );
			if (Math.abs(currentScore-score) < 0.0001){
				if (currentScore < score) {
					currentScore = score;
					currentW = newW;
				}
				break;
			} else if (currentScore < score) {
				currentScore = score;
				currentW = newW;
			}
			Collections.shuffle(indices);
			
		}
		return currentW;
	}
	
	double evaluate(double[] w){
		double metric = 0;

		for (int k = 0; k < trainingSet.size(); k++) {
			nextQuery();
			RetrievalCache.topicId = topic;
			RetrievalCache.indexName = index;
			reranker.start(query, index);
			reranker.start(w);	
			
			String[][] resultsSoFar = new String[10][5];
			for (int j = 0; j < 10; j++) {
				ResultSet resultSet = reranker.get();
				resultsSoFar[j] = resultSet.docnos;
				Feedback[] feedback = TrecUser.get(resultSet, topic);
				reranker.update(feedback);
			}
			metric += cubeTest.getAverageCubeTest(10, topic, resultsSoFar);
			//metric += ndcg.getNDCG(10, topic, resultsSoFar);
		}
		metric = metric/trainingSet.size();
		return metric;
	}
	
	
	double[] findMaxW(int d, double[] w){
		double step = 0.05;
		double bestLambdai = 0;
		double bestScore = -1;
		for (int i = 0; i < 20; i++) {
			w[d+2] = step*(i+1);
			double score = evaluate(w);
			if (score > bestScore){
				bestScore = score;
				bestLambdai = step*(i+1);
			}
		}
		w[d+2] = bestLambdai;
		return w;	
	}

	@Override
	public double validate(double[] weight) {
		double totalMetric = 0;

		for (String[] queryInfo: validationSet) {
			this.index = queryInfo[0];
			this.topic = queryInfo[1];
			this.query = queryInfo[2];
			String[][] result = new String[10][5];
			RetrievalCache.topicId = topic;
			RetrievalCache.indexName = index;
			reranker.start(query, index);
			reranker.start(weight);
			for (int i = 0; i < 10; i++) {
				ResultSet resultSet = reranker.get();
				result[i] = resultSet.docnos;
				Feedback[] feedback = TrecUser.get(resultSet, topic);
				reranker.update(feedback);
			}
			// System.out.println(topic + " " + cubeTest.getAverageCubeTest(n, topic, result));
			totalMetric += cubeTest.getAverageCubeTest(10, topic, result);
			//totalMetric += ndcg.getNDCG(10, topic, result);
		}
		if (totalMetric > 0) {
			totalMetric /= validationSet.size();
		}
		return totalMetric;
	}

	@Override
	public void setParam(double[] param) {
		lambda = param[0];
	}

	@Override
	public List<double[]> getParams() {
		List<Double> lambdas = new ArrayList<Double>();
		double step = 0.1;

		for (int i = 0; i < 9	; i++) {
			lambdas.add(step*(i+1));
		}
		

		List<List<Double>> lists = new ArrayList<List<Double>>();

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

	@Override
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
