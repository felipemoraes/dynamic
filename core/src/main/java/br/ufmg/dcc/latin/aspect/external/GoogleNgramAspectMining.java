package br.ufmg.dcc.latin.aspect.external;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.aspect.MostRelevantTermsAspectMining;
import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;

public class GoogleNgramAspectMining extends MostRelevantTermsAspectMining {

	public GoogleNgramAspectMining(int maxTerms){
		super(maxTerms);
		ExternalKnowledgeCache.init();
	}
	
	private float computeProb(String term){
		float sumTotalTermFreq = ExternalKnowledgeCache.ngram.getSumTotalTermFreq();
		float ttf = ExternalKnowledgeCache.ngram.getTotalTermFreq(term);
		return (float) Math.log(sumTotalTermFreq/(ttf+1));
	}
	

	public List<TermWeight> computeTermWeights(List<String> terms, String passage) {
		List<TermWeight> weights = new ArrayList<TermWeight>();
		for (String term : terms) {
			weights.add(new TermWeight(term,computeProb(term)));
		}
		return weights;
	}
}
