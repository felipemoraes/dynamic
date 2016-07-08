/**
 * 
 */
package br.ufmg.dcc.latin.searcher.matching;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.LMSimilarity.LMStats;

/**
 * @author Felipe Moraes
 *
 */
public class LMDirichletScorer extends LMDirichletSimilarity implements Scorer {
	/**
	 * @param f
	 */
	public LMDirichletScorer(float mu) {
		super(mu);
	}

	@Override
	public float totalScore(BasicStats[] basicStats, int[] termFreq, long docLen, int termCount) {
		float s = 0;
		for (int i = 0; i < termCount; i++) {
			LMStats lmStats = new LMStats("lm");
			float collectionProbability = 
					(basicStats[i].getTotalTermFreq() + 1F) / (basicStats[i].getNumberOfFieldTokens()+ 1F);
			//System.out.println("collectionProbability " + collectionProbability);
			//System.out.println("term weight " + (float)Math.log(1 + termFreq[i] /
       // (super.getMu() *collectionProbability )));
			//.out.println("term freq " + termFreq[i]);
			//System.out.println("document norm " + (float)Math.log(super.getMu() / (decodeNormValue(this.encodeNormValue(1, docLen)) + super.getMu())));
			lmStats.setCollectionProbability(collectionProbability);
			lmStats.setNumberOfDocuments(basicStats[i].getNumberOfDocuments());
			lmStats.setNumberOfFieldTokens(basicStats[i].getNumberOfFieldTokens());
			lmStats.setDocFreq(basicStats[i].getDocFreq());
			lmStats.setTotalTermFreq(basicStats[i].getTotalTermFreq());
			lmStats.setAvgFieldLength(basicStats[i].getAvgFieldLength());
			// we need to encode a normalization as lucene does that too
			s += score(lmStats, termFreq[i], decodeNormValue(this.encodeNormValue(1, docLen)));
			//System.out.println("Score " + score(lmStats, termFreq[i], decodeNormValue(this.encodeNormValue(1, docLen))));
		}
		return s;
	}
}
