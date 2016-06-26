package br.ufmg.dcc.latin.searcher.matching;

import org.apache.lucene.search.similarities.BasicStats;

public interface Scorer {
	
	public float totalScore(BasicStats[] basicStats, int[] termFreq, long docLen, int termCount);

}
