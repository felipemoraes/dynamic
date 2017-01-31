package org.apache.lucene.search.similarities;

public interface DynamicSimilarity {
	public float score(BasicStats stats, float freq, float docLen);
}
