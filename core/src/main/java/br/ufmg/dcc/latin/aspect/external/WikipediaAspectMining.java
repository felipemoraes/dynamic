package br.ufmg.dcc.latin.aspect.external;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.aspect.MostRelevantTermsAspectMining;
import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;
 
public class WikipediaAspectMining  extends MostRelevantTermsAspectMining {
	
	
	public WikipediaAspectMining(int maxTerms){
		super(maxTerms);
		ExternalKnowledgeCache.init();
	}

	
	private float computeIdf(String term){
		float numDocs = ExternalKnowledgeCache.wikipedia.getNumDocs();
		float docFreq = ExternalKnowledgeCache.wikipedia.getDocFreq(term);
		return (float) Math.log(numDocs/(docFreq+1));
	}
	

	public List<TermWeight> computeTermWeights(List<String> terms, String passage) {
		List<TermWeight> weights = new ArrayList<TermWeight>();
		for (String term : terms) {
			weights.add(new TermWeight(term,computeIdf(term)));
		}
		return weights;
	}

	
}
