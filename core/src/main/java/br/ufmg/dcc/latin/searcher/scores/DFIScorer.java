/**
 * 
 */
package br.ufmg.dcc.latin.searcher.scores;

import org.apache.lucene.search.similarities.DFISimilarity;
import org.apache.lucene.search.similarities.Independence;

/**
 * @author Felipe Moraes
 *
 */
public class DFIScorer extends DFISimilarity {

	/**
	 * @param independenceMeasure
	 */
	public DFIScorer(Independence independenceMeasure) {
		super(independenceMeasure);
		// TODO Auto-generated constructor stub
	}

}
