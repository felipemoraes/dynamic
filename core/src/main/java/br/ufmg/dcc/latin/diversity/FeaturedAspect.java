package br.ufmg.dcc.latin.diversity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

import gnu.trove.map.hash.TIntObjectHashMap;


public class FeaturedAspect  {
	TIntObjectHashMap<TermFeatures> termsFeatures;
	
	public FeaturedAspect(){
		termsFeatures = new TIntObjectHashMap<TermFeatures>();
	}

	public void putTerm(int termId, int passageId, int relevance) {
		if (!termsFeatures.containsKey(termId)){
			termsFeatures.put(termId, new TermFeatures(termId, passageId, relevance));
		}
		termsFeatures.get(termId).updateTerm(passageId, relevance);
	}

	
	public static TermFeatures[] findTopKHeap(TermFeatures[] arr, int k) { 
	    PriorityQueue<TermFeatures> pq = new PriorityQueue<TermFeatures>();
	    for (TermFeatures x : arr) { 
	        if (pq.size() < k) pq.add(x);
	        else if (pq.peek().compareTo(x) > 0) {
	            pq.poll();
	            pq.add(x);
	        }
	    }
	    TermFeatures[] res = new TermFeatures[k];
	    for (int i =0; i < k; i++) res[i] = pq.poll();
	    return res;

	}
	
	
	public TermFeatures[] getTopTerms(double[] weights) {
		
		TermFeatures[] candidateTerms = generateCandidateTerms(weights);
		
		int querySize = Math.min(20, candidateTerms.length);
		return findTopKHeap(candidateTerms,querySize);
		/*
		TIntObjectHashMap<TermFeatures> selectedTerms = new TIntObjectHashMap<>();
		while (selectedTerms.size() < querySize) {
			int maxRank = -1;
			double maxScore = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < candidateTerms.length; i++) {
				
				if (selectedTerms.containsKey(candidateTerms[i].termId)){
					continue;
				}
				
				
				if (maxScore < candidateTerms[i].weight){
					maxRank = i;
					maxScore = candidateTerms[i].weight;
				}
			}
	
			selectedTerms.put(candidateTerms[maxRank].termId, candidateTerms[maxRank]);
		}
		
		TermFeatures[] finalSelectedTerms = new TermFeatures[selectedTerms.size()];
		selectedTerms.values(finalSelectedTerms);
		
		return finalSelectedTerms;*/
	}
	
	public static double[] softmax(double[] vector) {
		// compute vector 2-norm
		double norm = 0.0;
		for (int i = 0; i < vector.length; i++) {
			norm += Math.exp(vector[i]) ;
		}
		

		if (norm == 0) {
			Arrays.fill(vector, 1);
		} else {
			for (int i = 0; i < vector.length; i++) {
				vector[i] = Math.exp(vector[i]) / norm;
			}
		}
		return vector;
	}


	private TermFeatures[] generateCandidateTerms(double[] weights) {
		
		TermFeatures[] termsCandidates = new TermFeatures[termsFeatures.size()];
		termsFeatures.values(termsCandidates);
	
		double[] maxFeatures = new double[8];
		double[] minFeatures = new double[8];
		for (int i = 0; i < minFeatures.length; i++) {
			double max = Double.MIN_VALUE;
			double min = Double.MAX_VALUE;
			for (int j = 0; j < termsCandidates.length; j++) {
				
				if (termsCandidates[j].features[i] < min) {
					min = termsCandidates[j].features[i];
				}
				if (termsCandidates[j].features[i] > max) {
					max = termsCandidates[j].features[i];
				}			
			}
			minFeatures[i] = min;
			maxFeatures[i] = max;
		}
		
		double[] weightsNorm = softmax(weights);
		for (int i = 0; i < termsCandidates.length; i++) {
			double weight = 0;
			double[] features = termsCandidates[i].features;
			for (int j = 0; j < minFeatures.length; j++) {
				if (maxFeatures[j]-minFeatures[j]  > 0) {
					double scaledFeature = (features[j] -  minFeatures[j])/(maxFeatures[j]-minFeatures[j]);
					weight += scaledFeature*weightsNorm[j];
				} else {
					weight += features[j]*weightsNorm[j];
				}
				
			}
			
			termsCandidates[i].weight = weight/features.length;
		}
		return termsCandidates;
	}
	
}
