package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParametersController {
	
	public static DynamicSystemParameters getParameters(String parametersFile){
		DynamicSystemParameters parameters = new DynamicSystemParameters();
		List<List<Float>> lists = new ArrayList<List<Float>>();
		try (BufferedReader br = new BufferedReader(new FileReader(parametersFile))) {
			String line =  br.readLine();
			parameters.reranker = line.replace("\n", "");
			while ((line = br.readLine()) != null) {
				List<Float> list = new ArrayList<Float>();
		    	String[] splitLine = line.split(",");
		    	for (int i = 0; i < splitLine.length; i++) {
					list.add(Float.parseFloat(splitLine[i]));
				}
		    	lists.add(list);
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		List<List<Float>> product = cartesianProduct(lists);
		List<float[]> experimentalParameters = new ArrayList<float[]>();
		for (List<Float> list : product) {
			int n = list.size();
			float[] params = new float[n];
			for (int i = 0; i < n; i++) {
				params[i] = list.get(i);
			}
		}
		parameters.experimentalParameters = experimentalParameters;
		return parameters;
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
