/**
 * 
 */
package br.ufmg.dcc.latin.searcher.matching;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.Distribution;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.Lambda;
import org.apache.lucene.search.similarities.Normalization;

/**
 * @author Felipe Moraes
 *
 */
public class IBScorer extends IBSimilarity implements Scorer {

	/**
	 * @param distribution
	 * @param lambda
	 * @param normalization
	 */
	public IBScorer(Distribution distribution, Lambda lambda, Normalization normalization) {
		super(distribution, lambda, normalization);
		// TODO Auto-generated constructor stub
	}

	@Override
	public float totalScore(BasicStats[] basicStats, long[] termFreq, long docLen, int termCount) {
		float s = 0;
	
		for (int i = 0; i < termCount; i++) {
			if (basicStats[i]==null) {
				System.out.println("te achei " + i);
			}
			// we need to encode a normalization as lucene does that too
			s += score(basicStats[i], termFreq[i], decodeNormValue(this.encodeNormValue(1, docLen)));
		}
		return s;
	}

}
