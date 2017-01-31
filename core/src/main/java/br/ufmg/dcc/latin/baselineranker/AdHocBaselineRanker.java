package br.ufmg.dcc.latin.baselineranker;

import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.search.similarities.DynamicSimilarity;
import org.apache.lucene.search.similarities.LMDirichlet;

import br.ufmg.dcc.latin.user.UserQuery;
import br.ufmg.dcc.latin.utils.RetrievalSystem;

public class AdHocBaselineRanker extends BaselineRanker {
	
	
	public AdHocBaselineRanker(String similarity, double[] fieldWeights){
		DynamicSimilarity dynamicSimilarity = null;
		if (similarity.equals("LM")) {
			dynamicSimilarity = new LMDirichlet(2500F);
		} else if (similarity.equals("DPH")){
			dynamicSimilarity = new DPH();
		}
		
		RetrievalSystem.similarity = dynamicSimilarity;
		RetrievalSystem.fieldWeights = fieldWeights;
		RetrievalSystem.buildQueryParser();
	}
	
	@Override
	public ResultList getResultList(UserQuery userQuery) {
		return RetrievalSystem.search(userQuery.query, userQuery.index);
	}

}
