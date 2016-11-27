package br.ufmg.dcc.latin.retrieval;

import java.util.HashMap;
import java.util.Map;

public class TermStatistics {
	
	private Map<String,Float> docFreq;
	private Map<String,Float> totalTermFreq;
	
	public TermStatistics(){
		docFreq = new HashMap<String,Float>();
		totalTermFreq = new HashMap<String,Float>();
	}
	
	public float docFreq(String term){
		return docFreq.getOrDefault(term, 0f);
	}
	
	public float totalTermFreq(String term){
		return totalTermFreq.getOrDefault(term, 0f);
	}
	
	public void docFreq(String term, float docFreq){
		this.docFreq.put(term, docFreq);
	}
	
	public void totalTermFreq(String term, float totalTermFreq){
		this.totalTermFreq.put(term, totalTermFreq);
	}
}
