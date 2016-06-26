/**
 * 
 */
package br.ufmg.dcc.latin.searcher.matching;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.util.SmallFloat;

/**
 * @author Felipe Moraes
 *
 */
public class TFIDFScorer implements Scorer {
	/** Norm to document length map. */
	  private static final float[] NORM_TABLE = new float[256];

	  static {
	    for (int i = 1; i < 256; i++) {
	      float floatNorm = SmallFloat.byte315ToFloat((byte)i);
	      NORM_TABLE[i] = 1.0f / (floatNorm * floatNorm);
	    }
	    NORM_TABLE[0] = 1.0f / NORM_TABLE[255]; // otherwise inf
	  }

	  protected float decodeNormValue(byte norm) {
		    return NORM_TABLE[norm & 0xFF];  // & 0xFF maps negative bytes to positive above 127
		  }
		  
		  /** Encodes the length to a byte via SmallFloat. */
		  protected byte encodeNormValue(float boost, float length) {
		    return SmallFloat.floatToByte315((boost / (float) Math.sqrt(length)));
		  }

	@Override
	public float totalScore(BasicStats[] basicStats, int[] termFreq, long docLen, int termCount) {
		float s = 0;
		float sumOfSquaredWeights = 0;
		for (int i = 0; i < termCount; i++) {
			float idf = (float)(Math.log((basicStats[i]
					.getNumberOfDocuments()+1)/(double)(basicStats[i].getDocFreq()+1)) + 1.0);
			sumOfSquaredWeights += Math.pow(idf, 2);
		}
		float queryNorm = (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
		float fieldNorm = (float) (1.0 / Math.sqrt(decodeNormValue(this.encodeNormValue(1, docLen))));
		for (int i = 0; i < termCount; i++) {
			// we need to encode a normalization as lucene does that too
			float idf = (float)(Math.log((basicStats[i]
					.getNumberOfDocuments()+1)/(double)(basicStats[i].getDocFreq()+1)) + 1.0);
			float tf = (float)Math.sqrt(termFreq[i]);
			
			
			s += idf*queryNorm*tf*fieldNorm*idf;
			
		}

		
		
		return s;
	}

}
