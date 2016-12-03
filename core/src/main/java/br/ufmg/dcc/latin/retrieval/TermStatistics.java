package br.ufmg.dcc.latin.retrieval;

import java.util.HashMap;
import java.util.Map;

public class TermStatistics {
	
	private Map<String,Double> docFreq;
	private Map<String,Double> totalTermFreq;
	
	public TermStatistics(){
		docFreq = new HashMap<String,Double>();
		totalTermFreq = new HashMap<String,Double>();
	}
	
	public double docFreq(String term){
		return docFreq.getOrDefault(term, 0d);
	}
	
	public double totalTermFreq(String term){
		return totalTermFreq.getOrDefault(term, 0d);
	}
	
	public void docFreq(String term, double docFreq){
		this.docFreq.put(term, docFreq);
	}
	
	public void totalTermFreq(String term, double totalTermFreq){
		this.totalTermFreq.put(term, totalTermFreq);
	}
}
