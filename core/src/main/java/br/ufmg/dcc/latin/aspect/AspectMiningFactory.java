package br.ufmg.dcc.latin.aspect;

import br.ufmg.dcc.latin.aspect.external.CollectionAspectMining;
import br.ufmg.dcc.latin.aspect.external.DBPediaEntitiesAspectMining;
import br.ufmg.dcc.latin.aspect.external.MSEntitiesAspectMining;
import br.ufmg.dcc.latin.aspect.external.GoogleNgramAspectMining;
import br.ufmg.dcc.latin.aspect.external.QuerylogAspectMining;
import br.ufmg.dcc.latin.aspect.external.WikipediaAspectMining;

public class AspectMiningFactory {
	
	public static AspectMining getInstance(String className, String index, int maxTerms){
		AspectMining aspectMining = null;

		if (className.equals("PassageAspectMining")){
			aspectMining = new PassageAspectMining();
		} else if (className.equals("WeightedPassageAspectMining")){
			aspectMining = new WeightedPassageAspectMining();
		} else if (className.equals("SubtopicNameAspectMining")){
			aspectMining = new SubtopicNameAspectMining();
		} else if (className.equals("MSEntitiesAspectMining")){
			aspectMining = new MSEntitiesAspectMining(maxTerms);
		} else if (className.equals("DBPediaEntitiesAspectMining")){
			aspectMining = new DBPediaEntitiesAspectMining(maxTerms);
		} else if (className.equals("QuerylogAspectMining")){
			aspectMining = new QuerylogAspectMining(maxTerms);
		} else if (className.equals("WikipediaAspectMining")){
			aspectMining = new WikipediaAspectMining(maxTerms);
		} else if (className.equals("GoogleNgramAspectMining")){
				aspectMining = new GoogleNgramAspectMining(maxTerms);
		
		} else if (className.equals("CollectionAspectMining")){
			aspectMining = new CollectionAspectMining(index, maxTerms);
		}else if (className.equals("OracleAspectMining")){
			aspectMining = new OracleAspectMining();
		}
		
		

		
		
		return aspectMining;
	}
}
