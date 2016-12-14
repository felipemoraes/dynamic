package br.ufmg.dcc.latin.learning;

import java.util.ArrayList;
import java.util.List;

public interface OnlineLearner {
	void setupReranker(String rerankerName);
	void loadTrainingSet(String trainingFilename);
	void loadValidationSet(String validationFilename);
	void nextQuery();
	double[] train();
	double validate(double[] weight);
	void setParam(double[] param);
	List<double[]> getParams();
	
	void dumpModel(String modelFile, double[] bestParam, double[] bestWeights);
	
	default <T> List<List<T>> cartesianProduct(List<List<T>> lists) {
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
