/**
 * 
 */
package br.ufmg.dcc.latin.searcher.matching;

import org.apache.lucene.search.similarities.AfterEffect;
import org.apache.lucene.search.similarities.BasicModel;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.Normalization;

/**
 * @author Felipe Moraes
 *
 */
public class DFRScorer extends DFRSimilarity implements Scorer {

	/**
	 * @param basicModel
	 * @param afterEffect
	 * @param normalization
	 */
	public DFRScorer(BasicModel basicModel, AfterEffect afterEffect, Normalization normalization) {
		super(basicModel, afterEffect, normalization);
		// TODO Auto-generated constructor stub
	}

	@Override
	public float totalScore(BasicStats[] basicStats, int[] termFreq, long docLen, int termCount) {
		float s = 0;
		for (int i = 0; i < termCount; i++) {
			// we need to encode a normalization as lucene does that too
			s += score(basicStats[i], termFreq[i], decodeNormValue(this.encodeNormValue(1, docLen)));
		}
		return s;
	}

}
