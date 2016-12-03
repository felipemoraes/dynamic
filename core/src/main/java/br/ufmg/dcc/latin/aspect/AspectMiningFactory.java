package br.ufmg.dcc.latin.aspect;

import br.ufmg.dcc.latin.aspect.external.CollectionAspectMining;
import br.ufmg.dcc.latin.aspect.external.DBPediaEntitiesAspectMining;
import br.ufmg.dcc.latin.aspect.external.MSEntitiesAspectMining;
import br.ufmg.dcc.latin.aspect.external.GoogleNgramAspectMining;
import br.ufmg.dcc.latin.aspect.external.QuerylogAspectMining;
import br.ufmg.dcc.latin.aspect.external.WikipediaAspectMining;

public class AspectMiningFactory {
	
	public static AspectMining getInstance(String className, String index){
		AspectMining aspectMining = null;

		if (className.equals("PassageAspectMining")){
			aspectMining = new PassageAspectMining();
		} else if (className.equals("WeightedPassageAspectMining")){
			aspectMining = new WeightedPassageAspectMining();
		} else if (className.equals("SubtopicNameAspectMining")){
			aspectMining = new SubtopicNameAspectMining();
		} else if (className.equals("MSEntitiesAspectMining")){
			aspectMining = new MSEntitiesAspectMining();
		} else if (className.equals("DBPediaEntitiesAspectMining")){
			aspectMining = new DBPediaEntitiesAspectMining();
		} else if (className.equals("QuerylogAspectMining")){
			aspectMining = new QuerylogAspectMining();
		} else if (className.equals("WikipediaAspectMining")){
			aspectMining = new WikipediaAspectMining();
		} else if (className.equals("GoogleNgramAspectMining")){
				aspectMining = new GoogleNgramAspectMining();
		
		} else if (className.equals("CollectionAspectMining")){
			aspectMining = new CollectionAspectMining(index);
		}
		

		
		
		return aspectMining;
	}
}
