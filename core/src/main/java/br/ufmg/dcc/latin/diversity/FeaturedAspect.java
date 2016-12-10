package br.ufmg.dcc.latin.diversity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.util.BytesRef;

public class FeaturedAspect  {
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

	public List<TermFeatures> getTopTerms(double[] weights) {
		List<TermFeatures> candidateTerms = generateCandidateTerms(weights);
		int querySize = Math.min(20, candidateTerms.size());
		Map<BytesRef,TermFeatures> selectedTerms = new HashMap<BytesRef,TermFeatures>();
		while (selectedTerms.size() < querySize) {
			int maxRank = -1;
			double maxScore = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < candidateTerms.size(); i++) {
				if (selectedTerms.containsKey(candidateTerms.get(i).term)){
					continue;
				}
				if (maxScore < candidateTerms.get(i).weight){
					maxRank = i;
				}
			}
			selectedTerms.put(candidateTerms.get(maxRank).term, candidateTerms.get(maxRank));
		}
		return (List<TermFeatures>) selectedTerms.values();
	}

	private List<TermFeatures> generateCandidateTerms(double[] weights) {
		List<TermFeatures> termsCandidates = (List<TermFeatures>) termsFeatures.values();
		double[] maxFeatures = new double[8];
		double[] minFeatures = new double[8];
		for (int i = 0; i < minFeatures.length; i++) {
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < termsFeatures.size(); j++) {
				if (termsFeatures.get(j).features[i] < min) {
					min = termsFeatures.get(j).features[i];
				}
				if (termsFeatures.get(j).features[i] > max) {
					max = termsFeatures.get(j).features[i];
				}			
			}
			minFeatures[i] = min;
			maxFeatures[i] = max;
		}
		for (int i = 0; i < termsCandidates.size(); i++) {
			double weight = 0;
			double[] features = termsCandidates.get(i).features;
			for (int j = 0; j < minFeatures.length; j++) {
				double scaledFeature = (features[j] -  minFeatures[j])/(maxFeatures[j]-minFeatures[j]);
				weight += scaledFeature*weights[j];
			}
			termsCandidates.get(i).weight = weight/features.length;
		}
		return termsCandidates;
	}
	
}
