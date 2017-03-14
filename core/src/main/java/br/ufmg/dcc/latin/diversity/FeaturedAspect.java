package br.ufmg.dcc.latin.diversity;

import java.util.Arrays;
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

	public void putTerm(int termId) {
		if (!termsFeatures.containsKey(termId)){
			termsFeatures.put(termId, new TermFeatures(termId));
		}
		termsFeatures.get(termId).updateTerm();
	}
	
	public static TermFeatures[] findTopKHeap(TermFeatures[] arr, int k) { 
	    PriorityQueue<TermFeatures> pq = new PriorityQueue<TermFeatures>();
	    for (TermFeatures x : arr) { 
	    	if (x.weight <= 0) continue;
	        if (pq.size() < k) pq.add(x);
	        else if (pq.peek().compareTo(x) > 0) {
	            pq.poll();
	            pq.add(x);
	        }
	    }
	    k = pq.size();
	    TermFeatures[] res = new TermFeatures[k];
	    for (int i =0; i < k; i++) {
	    	res[i] = pq.poll();
	    }
	    return res;

	}
	

	
	public TermFeatures[] getTopTerms(double[] weights) {
		TIntObjectHashMap<TermFeatures> collector = new TIntObjectHashMap<TermFeatures>();
		
		for (int i = 0; i < weights.length; i++) {
			if ( weights[i]  == 0) {
				continue;
			}
			TermFeatures[] candidateTerms = generateCandidateTerms(i);
			for (int j = 0; j < candidateTerms.length; j++) {
				collector.put(candidateTerms[j].termId, candidateTerms[j]);
			}
		}
		
		TermFeatures[] candidateTerms = new TermFeatures[collector.size()];
		collector.values(candidateTerms);
		
		//TermFeatures[] candidateTerms = new TermFeatures[termsFeatures.size()];
		//termsFeatures.values(candidateTerms);
		double[] maxValues = new double[weights.length];
		double[] minValues = new double[weights.length];
		Arrays.fill(minValues,Double.POSITIVE_INFINITY);
		
		
		for (int i = 0; i < candidateTerms.length; i++) {
			for (int j = 0; j < weights.length; j++) {
				if (candidateTerms[i].features[j] > maxValues[j]) {
					maxValues[j] = candidateTerms[i].features[j];
				}
				if (candidateTerms[i].features[j] < minValues[j]){
					minValues[j] = candidateTerms[i].features[j];
				}
			}
		}

		for (int i = 0; i < candidateTerms.length; i++) {
			candidateTerms[i].weight  = 0;
			for (int j = 0; j < weights.length; j++) {
				double scaledFeature = 1;
				double diff = (maxValues[j] - minValues[j]);
				if ( diff > 0) {
					scaledFeature = (candidateTerms[i].features[j] - minValues[j])/diff;
				}
				candidateTerms[i].weight += weights[j]*scaledFeature;
			}
		}
		
		
		int querySize = Math.min(30	, candidateTerms.length);
		
		TermFeatures[] topTerms = findTopKHeap(candidateTerms,querySize);
		double sum = 0;
		for (int i = 0; i < topTerms.length; i++) {
			sum += topTerms[i].weight;
		}
		
		for (int i = 0; i < topTerms.length; i++) {
			if (sum > 0) {
				topTerms[i].weight /= sum;
			} else{
				topTerms[i].weight = 0;
			}
			
		}
		
		return topTerms;
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


	private TermFeatures[] generateCandidateTerms(int f) {
		
		TermFeatures[] termsCandidates = new TermFeatures[termsFeatures.size()];
		termsFeatures.values(termsCandidates);
		
		for (int i = 0; i < termsCandidates.length; i++) {
			
			termsCandidates[i].weight = termsCandidates[i].features[f];
		}
		
		termsCandidates = findTopKHeap(termsCandidates, 100);

		return termsCandidates;
	}
	
	private TermFeatures[] generateCandidateTerms(int f, int passageId) {
		int size = 0;
		for (int term: termsFeatures.keys()) {
			if (termsFeatures.get(term).passageIds.contains(passageId)){
				size++;
			}
		}
		
		TermFeatures[] termsCandidates = new TermFeatures[size];
		
		int i = 0;
		for (int term: termsFeatures.keys()) {
			if (termsFeatures.get(term).passageIds.contains(passageId)) {
				
				termsCandidates[i] = new TermFeatures(termsFeatures.get(term).termId);
				termsCandidates[i].weight = termsFeatures.get(term).features[f];
				termsCandidates[i].features = termsFeatures.get(term).features;
				i++;	
				
			}
		}
		
		termsCandidates = findTopKHeap(termsCandidates, 100);

		return termsCandidates;
	}

	public TermFeatures[] getTopTerms(double[] weights, int passageId) {
		TIntObjectHashMap<TermFeatures> collector = new TIntObjectHashMap<TermFeatures>();
		
		for (int i = 0; i < weights.length; i++) {
			if ( weights[i]  == 0) {
				continue;
			}
			TermFeatures[] candidateTerms = generateCandidateTerms(i,passageId);
			for (int j = 0; j < candidateTerms.length; j++) {
				collector.put(candidateTerms[j].termId, candidateTerms[j]);
			}
		}
		
		TermFeatures[] candidateTerms = new TermFeatures[collector.size()];
		collector.values(candidateTerms);
		
		//TermFeatures[] candidateTerms = new TermFeatures[termsFeatures.size()];
		//termsFeatures.values(candidateTerms);
		double[] maxValues = new double[weights.length];
		double[] minValues = new double[weights.length];
		Arrays.fill(minValues,Double.POSITIVE_INFINITY);
		
		
		for (int i = 0; i < candidateTerms.length; i++) {
			for (int j = 0; j < weights.length; j++) {
				
				if (candidateTerms[i].features[j] > maxValues[j]) {
					maxValues[j] = candidateTerms[i].features[j];
				}
				if (candidateTerms[i].features[j] < minValues[j]){
					minValues[j] = candidateTerms[i].features[j];
				}
			}
		}

		for (int i = 0; i < candidateTerms.length; i++) {
			candidateTerms[i].weight  = 0;
			for (int j = 0; j < weights.length; j++) {
				double scaledFeature = 1;
				double diff = (maxValues[j] - minValues[j]);
				if ( diff > 0) {
					scaledFeature = (candidateTerms[i].features[j] - minValues[j])/diff;
				}
				candidateTerms[i].weight += weights[j]*scaledFeature;
			}
		}
		
		
		int querySize = Math.min(30	, candidateTerms.length);
		
		TermFeatures[] topTerms = findTopKHeap(candidateTerms,querySize);
		double sum = 0;
		for (int i = 0; i < topTerms.length; i++) {
			sum += topTerms[i].weight;
		}
		
		for (int i = 0; i < topTerms.length; i++) {
			if (sum > 0) {
				topTerms[i].weight /= sum;
			} else{
				topTerms[i].weight = 0;
			}
			
		}
		
		return topTerms;
	}
	
}
