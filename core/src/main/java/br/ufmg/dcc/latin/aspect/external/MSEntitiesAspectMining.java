package br.ufmg.dcc.latin.aspect.external;

import java.util.List;

import br.ufmg.dcc.latin.aspect.MostRelevantTermsAspectMining;
import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;

public class MSEntitiesAspectMining extends MostRelevantTermsAspectMining {
	public MSEntitiesAspectMining(int maxTerms){
		super(maxTerms);
		ExternalKnowledgeCache.init();
	}
	
	public List<TermWeight> computeTermWeights(List<String> terms, String passage) {
		return ExternalKnowledgeCache.msEntityLinkingCollection.getTermWeights(passage);
	}
	

}
