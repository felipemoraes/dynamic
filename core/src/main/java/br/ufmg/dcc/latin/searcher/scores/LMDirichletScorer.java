/**
 * 
 */
package br.ufmg.dcc.latin.searcher.scores;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;

/**
 * @author Felipe Moraes
 *
 */
public class LMDirichletScorer extends LMDirichletSimilarity {
	/**
	 * @param f
	 */
	public LMDirichletScorer(float mu) {
		super(mu);
	}

	public float totalScore(BasicStats stats, float freq, float docLen){
		
		return this.score(stats, freq, docLen);
	}
}
