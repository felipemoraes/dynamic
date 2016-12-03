package br.ufmg.dcc.latin.aspect.external;

import java.util.List;

import br.ufmg.dcc.latin.aspect.MostRelevantTermsAspectMining;
import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;

public class DBPediaEntitiesAspectMining extends MostRelevantTermsAspectMining {
	public DBPediaEntitiesAspectMining(int maxTerms){
		super(maxTerms);
		ExternalKnowledgeCache.init();
	}
	
	public List<TermWeight> computeTermWeights(List<String> terms, String passage) {
		return ExternalKnowledgeCache.dbpediaEntityLinkingCollection.getTermWeights(passage);
	}
	
}
