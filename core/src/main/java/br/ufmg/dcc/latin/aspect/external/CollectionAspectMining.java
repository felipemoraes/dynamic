package br.ufmg.dcc.latin.aspect.external;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.aspect.MostRelevantTermsAspectMining;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class CollectionAspectMining extends MostRelevantTermsAspectMining {
	String index;
	
	public CollectionAspectMining(String index,int maxTerms){
		super(maxTerms);
		this.index = index;
	}
	
	private double computeIdf(String term){
		BytesRef termRef = new BytesRef(term);
		float[] weights = RetrievalController.getFiedlWeights();
		double idf = weights[0]*RetrievalController.getIdf(index, "title", termRef) + weights[1]*RetrievalController.getIdf(index, "content", termRef);
		return idf ;
		
	}
	

	public List<TermWeight> computeTermWeights(List<String> terms, String passage) {
		List<TermWeight> weights = new ArrayList<TermWeight>();
		for (String term : terms) {
			weights.add(new TermWeight(term, computeIdf(term)));
		}
		return weights;
	}
}
