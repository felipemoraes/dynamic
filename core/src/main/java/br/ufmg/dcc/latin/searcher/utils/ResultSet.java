package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;
import java.util.Map.Entry;

public class ResultSet {
	
	private HashMap<String,Double> resultSet;
	private HashMap<String,HashMap<String, Double>> details;
	
	public ResultSet(){
		this.resultSet = new HashMap<String,Double>();
		this.setDetails(new HashMap<String,HashMap<String, Double>>());
	}
	
	public ResultSet(HashMap<String,Double> resultSet){
		this.setResultSet(resultSet);
	}

	public HashMap<String,Double> getResultSet() {
		return resultSet;
	}

	public void setResultSet(HashMap<String,Double> resultSet) {
		this.resultSet = resultSet;
	}
	
	public void putResult(String docId, Double score){
		resultSet.put(docId, score);
	}
	
	public String toString(){
		String str = "";
		for (Entry<String, Double> result : resultSet.entrySet()) {
			str += result.getKey() + ":" + result.getValue().toString() + " ";
		}
		return str;
	}

	public HashMap<String,HashMap<String, Double>> getDetails() {
		return details;
	}

	public void setDetails(HashMap<String,HashMap<String, Double>> details) {
		this.details = details;
	}
}
	

