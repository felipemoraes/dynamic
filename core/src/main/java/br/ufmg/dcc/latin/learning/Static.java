package br.ufmg.dcc.latin.learning;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.dynamicsystem.TrecUser;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.metrics.CubeTest;
import br.ufmg.dcc.latin.metrics.nDCG;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;
import br.ufmg.dcc.latin.reranking.InteractiveRerankerFactory;

public class Static implements Learner {

	
	private List<String[]> trainingSet;
	private List<String[]> validationSet;
	
	private int n;
	private String index;
	private String topic;
	private String query;
	
	private double lambda;
	
	InteractiveReranker reranker;
	
	int nextIndex;
	
	@Override
	public void setupReranker(String rerankerName) {
		reranker = InteractiveRerankerFactory.getInstance(rerankerName, "PassageAspectMining");
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

	@Override
	public double[] train() {
		double[] weights = new double[10];
		weights[0] = 1000d;
		weights[1] = lambda;
		for (int i = 2; i < weights.length; i++) {
			weights[i] = 0;
		}
		weights[4] = 1;
		// 0.5 0.5 0.5 0.55 0.9 0.5 0.45 0.5
		/*weights[2] = 0.5; // 1
		weights[3] = 0.5;
		weights[4] = 0.5;
		weights[5] = 0.55;
		weights[6] = 0.9;
		weights[7] = 0.5;
		weights[8] = 0.45;
		weights[9] = 0.5;*/
		return weights;
	}

	@Override
	public double validate(double[] weight) {
		double totalMetric = 0;
		nDCG nDCG = new nDCG();
		CubeTest cubeTest = new CubeTest();
		for (String[] queryInfo: validationSet) {
			this.index = queryInfo[0];
			this.topic = queryInfo[1];
			this.query = queryInfo[2];
			//if (!topic.equals("DD16-1")){
			//	continue;
			//}
			//System.out.println(query);
			String[][] result = new String[n][5];
			RetrievalCache.topicId = topic;
			RetrievalCache.indexName = index;
			reranker.start(query, index);
			reranker.start(weight);
			reranker.setParams(weight);
			for (int i = 0; i < 10; i++) {
				
				ResultSet resultSet = reranker.get();
				result[i] = resultSet.docnos;
				Feedback[] feedback = TrecUser.get(resultSet, topic);
				reranker.update(feedback);
			}

			
		//	System.out.println(topic + " " + nDCG.getNDCG(n, topic, result));
		//	totalMetric += nDCG.getNDCG(n, topic, result);
				System.out.println(topic + " " + cubeTest.getAverageCubeTest(n, topic, result));
				totalMetric +=  cubeTest.getAverageCubeTest(n, topic, result);
		}
		if (totalMetric > 0) {
			totalMetric /= validationSet.size();
		}
		return totalMetric;
	}

	@Override
	public void setParam(double[] param) {
		lambda = param[0];
		n = 10;
	}

	@Override
	public List<double[]> getParams() {

		List<Double> lambdas = new ArrayList<Double>();
		double step = 0.1;

		for (int i = 0; i < 10	; i++) {
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
