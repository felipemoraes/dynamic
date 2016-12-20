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
			//if (aspectId.equals("DD16-1.3")) {
			//	System.out.print(RetrievalController.vocab[0].getTerm(terms[i]) + " ");
			//}
		}
		//if (aspectId.equals("DD16-1.3")) {
		//	System.out.println(terms.length);
		//}
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
		TermFeatures[] topTerms = termsFeatures.getTopTerms(weights);
		TIntDoubleHashMap complexQuery = new TIntDoubleHashMap();
		
		for (int i = 0; i < topTerms.length; i++) {
			double weight = topTerms[i].weight;

			//System.out.print(RetrievalController.vocab[0].getTerm(topTerms[i].termId) + " " + weight + " ");
			complexQuery.put(topTerms[i].termId, 1);
		}
		//System.out.println();
		return complexQuery;
	}
}
