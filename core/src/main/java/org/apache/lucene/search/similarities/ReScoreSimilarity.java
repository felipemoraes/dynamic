package org.apache.lucene.search.similarities;

public interface ReScoreSimilarity {
	public float score(BasicStats stats, float freq, float docLen);
}
