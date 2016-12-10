package br.ufmg.dcc.latin.diversity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class FeaturedAspectModel {
	
	private Map<String, FeaturedAspect> featuredAspectes;
	
	public FeaturedAspectModel(){
		featuredAspectes = new HashMap<String, FeaturedAspect>();
	}

	public void addToAspect(String aspectId, int passageId, int relevance) {
		if (!featuredAspectes.containsKey(aspectId)){
			featuredAspectes.put(aspectId, new FeaturedAspect());
		}
		Terms passageTerms = RetrievalController.getPassageTerms(passageId);
		try {
			TermsEnum terms = passageTerms.iterator();
			BytesRef term = terms.next();
			while( term != null) {
				featuredAspectes.get(aspectId).putTerm(term,passageId,relevance);
				term = terms.next();
			}
		} catch (IOException e) {
			e.printStackTrace();
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

	public String getAspectQuery(String aspectId, double[] weights) {
		
		FeaturedAspect termsFeatures = featuredAspectes.get(aspectId);
		List<TermFeatures> topTerms = termsFeatures.getTopTerms(weights);
		String query = "";
		for (int i = 0; i < topTerms.size(); i++) {
			query += topTerms.get(i).term.utf8ToString() + " ";
		}
		return query;
	}
}
