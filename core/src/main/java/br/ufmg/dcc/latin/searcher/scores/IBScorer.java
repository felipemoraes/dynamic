/**
 * 
 */
package br.ufmg.dcc.latin.searcher.scores;

import org.apache.lucene.search.similarities.Distribution;
import org.apache.lucene.search.similarities.IBSimilarity;
import org.apache.lucene.search.similarities.Lambda;
import org.apache.lucene.search.similarities.Normalization;

/**
 * @author Felipe Moraes
 *
 */
public class IBScorer extends IBSimilarity {

	/**
	 * @param distribution
	 * @param lambda
	 * @param normalization
	 */
	public IBScorer(Distribution distribution, Lambda lambda, Normalization normalization) {
		super(distribution, lambda, normalization);
		// TODO Auto-generated constructor stub
	}

}
