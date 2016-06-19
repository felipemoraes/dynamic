/**
 * 
 */
package br.ufmg.dcc.latin.searcher.scores;

import org.apache.lucene.search.similarities.LMJelinekMercerSimilarity;

/**
 * @author Felipe Moraes
 *
 */
public class LMJelinekMercerScorer extends LMJelinekMercerSimilarity {

	/**
	 * @param collectionModel
	 * @param lambda
	 */
	public LMJelinekMercerScorer(CollectionModel collectionModel, float lambda) {
		super(collectionModel, lambda);
		// TODO Auto-generated constructor stub
	}

}
