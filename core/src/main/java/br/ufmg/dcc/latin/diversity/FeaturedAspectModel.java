package br.ufmg.dcc.latin.diversity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.ufmg.dcc.latin.index.DocVec;
import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class FeaturedAspectModel {
	
	private Map<String, FeaturedAspect> featuredAspectes;
	
	public FeaturedAspectModel(){
		featuredAspectes = new HashMap<String, FeaturedAspect>();
	}

	public void addToAspect(String aspectId, int passageId, int relevance) {
		if (!featuredAspectes.containsKey(aspectId)){
			featuredAspectes.put(aspectId, new FeaturedAspect());
		}
		
		DocVec passageDocVec = RetrievalController.getPassageTerms(passageId);
		int[] terms = passageDocVec.vec.keys();
		for (int i = 0; i < terms.length; i++) {
			
			featuredAspectes.get(aspectId).putTerm(terms[i],passageId,relevance);
		}
	}

	public int numAspects() {
		return featuredAspectes.keySet().size();
	}

	public List<String> getAspects() {
		List<String> aspects = new ArrayList<String>();
		for (String aspect : featuredAspectes.keySet()) {
			aspects.add(aspect);
		}
		return aspects;
		
	}

	public TIntDoubleHashMap getAspectQuery(String aspectId, double[] weights) {
		
		FeaturedAspect termsFeatures = featuredAspectes.get(aspectId);
		List<TermFeatures> topTerms = termsFeatures.getTopTerms(weights);
		TIntDoubleHashMap complexQuery = new TIntDoubleHashMap();
		for (int i = 0; i < topTerms.size(); i++) {
			double weight = topTerms.get(i).weight;
			if (weight < 0) {
				weight = 0;
			}
			complexQuery.put(topTerms.get(i).termId, weight);
			
		}
		return complexQuery;
	}
}
