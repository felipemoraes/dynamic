package br.ufmg.dcc.latin.diversity;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.util.BytesRef;

public class FeaturedAspect {
	Map<BytesRef, TermFeatures> termsFeatures;
	
	public FeaturedAspect(){
		termsFeatures = new HashMap<BytesRef, TermFeatures>();
	}

	public void putTerm(BytesRef term, int passageId, int relevance) {
		if (!termsFeatures.containsKey(term)){
			termsFeatures.put(term, new TermFeatures(term, passageId, relevance));
		}
		termsFeatures.get(term).updateTerm(passageId, relevance);
	}
}
