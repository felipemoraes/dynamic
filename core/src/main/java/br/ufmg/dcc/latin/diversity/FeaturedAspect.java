package br.ufmg.dcc.latin.diversity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FeaturedAspect  {
	Map<String, TermFeatures> termsFeatures;
	
	public FeaturedAspect(){
		termsFeatures = new HashMap<String, TermFeatures>();
	}

	public void putTerm(String term, int passageId, int relevance) {
		if (!termsFeatures.containsKey(term)){
			termsFeatures.put(term, new TermFeatures(term, passageId, relevance));
		}
		termsFeatures.get(term).updateTerm(passageId, relevance);
	}

	public List<TermFeatures> getTopTerms(double[] weights) {
		List<TermFeatures> candidateTerms = generateCandidateTerms(weights);
		
		int querySize = Math.min(20, candidateTerms.size());
		Map<String,TermFeatures> selectedTerms = new HashMap<String,TermFeatures>();
		while (selectedTerms.size() < querySize) {
			int maxRank = -1;
			double maxScore = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < candidateTerms.size(); i++) {
				
				if (selectedTerms.containsKey(candidateTerms.get(i).term)){
					continue;
				}
				
				
				if (maxScore < candidateTerms.get(i).weight){
					maxRank = i;
					maxScore = candidateTerms.get(i).weight;
				}
			}
			
			selectedTerms.put(candidateTerms.get(maxRank).term, candidateTerms.get(maxRank));
		}
		List<TermFeatures> finalSelectedTerms = new ArrayList<TermFeatures>();
		for (TermFeatures termFeatures : selectedTerms.values()) {
			finalSelectedTerms.add(termFeatures);
		}
		return finalSelectedTerms;
	}

	private List<TermFeatures> generateCandidateTerms(double[] weights) {
		
		List<TermFeatures> termsCandidates = new ArrayList<TermFeatures>();
		for (TermFeatures termFeatures: termsFeatures.values()) {
			termsCandidates.add(termFeatures);
		}
	
		double[] maxFeatures = new double[8];
		double[] minFeatures = new double[8];
		for (int i = 0; i < minFeatures.length; i++) {
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < termsCandidates.size(); j++) {
				
				if (termsCandidates.get(j).features[i] < min) {
					min = termsCandidates.get(j).features[i];
				}
				if (termsCandidates.get(j).features[i] > max) {
					max = termsCandidates.get(j).features[i];
				}			
			}
			minFeatures[i] = min;
			maxFeatures[i] = max;
		}
		for (int i = 0; i < termsCandidates.size(); i++) {
			double weight = 0;
			double[] features = termsCandidates.get(i).features;
			for (int j = 0; j < minFeatures.length; j++) {
				if (maxFeatures[j]-minFeatures[j]  > 0) {
					double scaledFeature = (features[j] -  minFeatures[j])/(maxFeatures[j]-minFeatures[j]);
					weight += scaledFeature*weights[j];
				} else {
					weight += features[j]*weights[j];
				}
				
			}
			
			termsCandidates.get(i).weight = weight/features.length;
		}
		return termsCandidates;
	}
	
}
