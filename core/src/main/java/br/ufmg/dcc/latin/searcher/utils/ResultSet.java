package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;
import java.util.Map.Entry;

public class ResultSet {
	
	private HashMap<String,Double> resultSet;
	
	private Details details;
	
	public ResultSet(){
		this.resultSet = new HashMap<String,Double>();
		this.setDetails(new Details());
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

	public Details getDetails() {
		return details;
	}

	public void setDetails(Details details) {
		this.details = details;
	}




}
	

