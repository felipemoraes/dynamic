package br.ufmg.dcc.latin.aspect.external;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.aspect.MostRelevantTermsAspectMining;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class CollectionAspectMining extends MostRelevantTermsAspectMining {
	String index;
	
	public CollectionAspectMining(String index){
		this.index = index;
	}
	
	private float computeIdf(String term){
		float[] weights = RetrievalController.getFiedlWeights();
		float idf = weights[0]*RetrievalController.getIdf(index, "title", term) + weights[1]*RetrievalController.getIdf(index, "content", term);
		if (Float.isNaN(idf)) {
			System.out.println("Computing idf");
		}
		
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
