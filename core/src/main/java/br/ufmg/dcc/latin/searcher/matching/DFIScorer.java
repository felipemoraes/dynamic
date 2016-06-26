/**
 * 
 */
package br.ufmg.dcc.latin.searcher.matching;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.DFISimilarity;
import org.apache.lucene.search.similarities.Independence;

/**
 * @author Felipe Moraes
 *
 */
public class DFIScorer extends DFISimilarity implements Scorer{

	/**
	 * @param independenceMeasure
	 */
	public DFIScorer(Independence independenceMeasure) {
		super(independenceMeasure);
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
