/**
 * 
 */
package br.ufmg.dcc.latin.searcher.scores;

import org.apache.lucene.search.similarities.AfterEffect;
import org.apache.lucene.search.similarities.BasicModel;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.Normalization;

/**
 * @author Felipe Moraes
 *
 */
public class DFRScorer extends DFRSimilarity {

	/**
	 * @param basicModel
	 * @param afterEffect
	 * @param normalization
	 */
	public DFRScorer(BasicModel basicModel, AfterEffect afterEffect, Normalization normalization) {
		super(basicModel, afterEffect, normalization);
		// TODO Auto-generated constructor stub
	}

}
