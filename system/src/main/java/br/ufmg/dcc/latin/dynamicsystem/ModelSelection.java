package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class ModelSelection {
	

	public static void main(String[] args) throws IOException {
		String trainingFilename = args[0];
		String validationFilename = args[1];
		String modelFile = args[2];
		
		TrecUser.load("../share/truth_data.txt");
		
		double[] bestParam = null;
		double[] bestWeights = null;
		double bestScore = -1;
		DBGD learner = new DBGD();
		learner.setupReranker(args[3]);
		learner.loadTrainingSet(trainingFilename);
		learner.loadValidationSet(validationFilename);
		int i = 0;
		List<double[]> params = getParams();
		for (double[] param: params){
			learner.setParam(param);
			double[] weights = learner.train();
			double score = learner.validate(weights);
			System.out.println("Score found: " + score);
			if (score > bestScore) {
				System.out.println("Best score found it " + score);
				bestScore = score;
				bestParam = param;
				bestWeights = weights;
				dumpModel(modelFile, bestParam,bestWeights);
			}
			i++;
			System.out.println("Processed " + i + " of " + params.size() );
			
		}
		
		dumpModel(modelFile, bestParam,bestWeights);
	}
	
	private static void dumpModel(String modelFile, double[] bestParam, double[] bestWeights) {
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


	public static List<double[]> getParams(){
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
		deltas.add(0.2d);
		step = 0.1;
		for (int i = 0; i < 10	; i++) {
			lambdas.add(step*(i+1));
		}
		iterations.add(100d);

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
	
	public static <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
	    List<List<T>> resultLists = new ArrayList<List<T>>();
	    if (lists.size() == 0) {
	        resultLists.add(new ArrayList<T>());
	        return resultLists;
	    } else {
	        List<T> firstList = lists.get(0);
	        List<List<T>> remainingLists = cartesianProduct(lists.subList(1, lists.size()));
	        for (T condition : firstList) {
	            for (List<T> remainingList : remainingLists) {
	                ArrayList<T> resultList = new ArrayList<T>();
	                resultList.add(condition);
	                resultList.addAll(remainingList);
	                resultLists.add(resultList);
	            }
	        }
	    }
	    return resultLists;
	}

}
