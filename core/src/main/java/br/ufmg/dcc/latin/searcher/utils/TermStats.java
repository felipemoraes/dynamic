/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Felipe Moraes
 *
 */
public class TermStats {
	
	// field, term, stats
	private Map<String,Terms> stats;
	
	public TermStats(){
		stats = new HashMap<String,Terms>();
	}
	
	private void checkFieldAndTermExistAndThenCreate(String field, String term){
		if (!stats.containsKey(field)) {
			stats.put(field, new Terms());
		}
		
		if (!stats.get(field).terms.containsKey(term)) {
			stats.get(field).terms.put(term, new Stats());
		}
	}
	
	public class Terms {
		public Terms (){
			terms = new HashMap<String,Stats>();
		}
		
		private Map<String,Stats> terms;
	}
	
	public class Stats {
		private long docFreq;
		private long totalTermFreq;
	}

	public long getDocFreq(String field, String term){
		return stats.get(field).terms.get(term).docFreq;
	}
	
	public long getTotalTermFreq(String field, String term){
		return stats.get(field).terms.get(term).totalTermFreq;
	}
	
	public void setDocFreq(String field, String term, long docFreq){
		checkFieldAndTermExistAndThenCreate(field,term);
		stats.get(field).terms.get(term).docFreq = docFreq;
	}
	
	public void setTotalTermFreq(String field, String term, long totalTermFreq){
		checkFieldAndTermExistAndThenCreate(field,term);
		stats.get(field).terms.get(term).totalTermFreq = totalTermFreq;
	}
}
