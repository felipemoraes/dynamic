package br.ufmg.dcc.latin.cache;

import br.ufmg.dcc.latin.external.EntityLinkingCollection;
import br.ufmg.dcc.latin.external.ExternalCollection;
import br.ufmg.dcc.latin.external.NgramCollection;

public class ExternalKnowledgeCache {
	
	public static ExternalCollection wikipedia;
	public static ExternalCollection queryLog;
	
	public static NgramCollection ngram;
	
	public static EntityLinkingCollection msEntityLinkingCollection;
	public static EntityLinkingCollection dbpediaEntityLinkingCollection;
	
	public static void init(){
		if (wikipedia == null ){ 
			wikipedia = new ExternalCollection("../share/wikipedia_counts.txt");
		}
		
		if (queryLog == null) {
			queryLog = new ExternalCollection("../share/msn_counts.txt");
		}
		
		if (msEntityLinkingCollection == null) {
			msEntityLinkingCollection = new EntityLinkingCollection("../share/MicrosoftEntityLinkingData.txt");
		}
		
		if (dbpediaEntityLinkingCollection == null) {
			dbpediaEntityLinkingCollection = new EntityLinkingCollection("../share/DBPediaEntityLinkingData.txt");
		}
		
		if (ngram == null) {
			ngram = new NgramCollection();
		}
		
	}

}
