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
public class FieldStats {

	// field
	private Map<String,Stats> stats;
	
	public FieldStats(){
		stats = new HashMap<String,Stats>();
	}
	
	public void putStats(String field, long docCount, long sumTotalTermFreq, long sumDocFreq ){
		stats.get(field).setDocCount(docCount);
		stats.get(field).setSumTotalTermFreq(sumTotalTermFreq);
		stats.get(field).setSumDocFreq(sumDocFreq);
	}
	
	private void checkFieldExistsAndThenCreate(String field){
		if (!stats.containsKey(field)) {
			stats.put(field, new Stats());
		}
	}
	public void setDocCount(String field, Long docCount) {
		checkFieldExistsAndThenCreate(field);
		stats.get(field).docCount = docCount;
	}
	
	public void setSumTotalTermFreq(String field, Long sumTotalTermFreq) {
		checkFieldExistsAndThenCreate(field);
		stats.get(field).sumTotalTermFreq = sumTotalTermFreq;
	}
	
	public void setSumDocFreq(String field, Long sumDocFreq){
		checkFieldExistsAndThenCreate(field);
		stats.get(field).sumDocFreq = sumDocFreq;
	}

	
	public long getDocCount(String field) {
		return stats.get(field).getDocCount();
	}
	public long getSumTotalTermFreq(String field) {
		return stats.get(field).getSumTotalTermFreq();
	}
	
	public long getSumDocFreq(String field){
		return stats.get(field).getSumDocFreq();
	}

	
	private class Stats {
		
		private long docCount;
		private long sumTotalTermFreq;
		private long sumDocFreq;
		
		public long getDocCount() {
			return docCount;
		}
		public void setDocCount(long docCount) {
			this.docCount = docCount;
		}
		public long getSumTotalTermFreq() {
			return sumTotalTermFreq;
		}
		public void setSumTotalTermFreq(long sumTotalTermFreq) {
			this.sumTotalTermFreq = sumTotalTermFreq;
		}
		public long getSumDocFreq() {
			return sumDocFreq;
		}
		public void setSumDocFreq(long sumDocFreq) {
			this.sumDocFreq = sumDocFreq;
		}
	}

}
