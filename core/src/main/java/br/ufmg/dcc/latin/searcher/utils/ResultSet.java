package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;

public class ResultSet {
	
	private HashMap<String,Double> resultSet;
	
	public ResultSet(){
		
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
	
}
	

