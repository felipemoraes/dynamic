package org.apache.lucene.search.similarities;

import org.apache.lucene.search.similarities.LMSimilarity.LMStats;

public class BoostedBasicStats extends LMStats {

	public BoostedBasicStats(String field) {
		super(field);
	}
	
	 public void setBoost(float boost) {
		 this.boost = boost;
	 }
}
