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
public class BM25Scorer implements Scorer {
	/** Norm to document length map. */
	  private static final float[] NORM_TABLE = new float[256];

	  private float k1;
	  private float b;
	  public BM25Scorer(float k1, float b){
		  this.k1 = k1;
		  this.b = b;
	  }
	  public BM25Scorer(){
		  this(1.2f, 0.75f);
	  }
	  
	  
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
		for (int i = 0; i < termCount; i++) {
			// we need to encode a normalization as lucene does that too
			
			float idf = (float) Math.log(1 + (basicStats[i]
					.getNumberOfDocuments() - basicStats[i].getDocFreq() 
					+ 0.5D)/(basicStats[i].getDocFreq() + 0.5D));
			float fieldLength = decodeNormValue(this.encodeNormValue(1, docLen));
			
			float tfNorm = (termFreq[i] * (k1+1));

			tfNorm /= (termFreq[i] + k1*(1-b+b*fieldLength/basicStats[i].getAvgFieldLength()));

			 s += tfNorm*idf;
		}
		
		return s;
	}

}
