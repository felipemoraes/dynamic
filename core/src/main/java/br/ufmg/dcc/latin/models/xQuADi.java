package br.ufmg.dcc.latin.models;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


public class xQuADi extends Model{

	private double[] novelty;
	private HashSet<Integer> selected;
	private HashMap<Integer,Double> currentSelected;
	
	public xQuADi(){
		novelty = new double[100];
		selected = new HashSet<Integer>();
		currentSelected = new HashMap<Integer,Double>();
		Arrays.fill(novelty, -1);
	}
	
	public Map<String, Double> get(xQuADiParameter param){
		currentSelected.clear();
		//FIXME
		
		score(param.docIds,param.relevance,param.coverage,
				param.importance,param.ambiguity,param.depth, param.k);
		for (int i = 0; i < param.relevance.length; i++) {
			//System.out.print(param.relevance[i] + " ");
		}
		//System.out.println();
		
		HashMap<String,Double> result = new HashMap<String,Double>();
		for (int i = 0; i < param.docIds.length; ++i){
			if (currentSelected.containsKey(param.docIds[i])) {
				result.put(param.docNos[i],currentSelected.get(param.docIds[i]));
			}
		}

		return result;
	}
	
	public double[] score(int[] docids, double[] relevance,
			double[][] coverage, double[] importance, double ambiguity, int depth, int k) {
		
		
		int n = docids.length;
		
		// depth size now is updated
		depth += selected.size();
		
		if (k == 0) {
			
			if (selected.size() == docids.length){
				return relevance;
			}
			while(selected.size() < depth){
				for (int i = 0; i < n; i++) {
					if (!selected.contains(docids[i])) {
						selected.add(docids[i]);
						currentSelected.put(docids[i], relevance[i]);
					}
					if (selected.size() == depth){
						break;
					}
				}
			}
			
			return relevance;
		}
		
		
		for (int i = 0; i < k; i++) {
			novelty[i] = 1;
		}
		
		
		
		for (int i = 0; i < n; i++) {
			if (selected.contains(docids[i])){
				
				for (int j = 0; j < k; j++) {
					novelty[j] *= Math.pow(0.9, (double) 1 - coverage[i][j]);
				}
			}
		}

		for (int i = 0; i < k; i++) {
			System.out.println(novelty[i]);
		}
		System.out.println();


		double[] scores = new double[n];
		Arrays.fill(scores, 0);
		for (int i = 0; i < relevance.length; i++) {
		//	System.out.print(i + ":" + relevance[i] + " ");
		}
		//System.out.println();
		
		// greedily diversify the all documents
		while (selected.size() < depth) {
			int maxRank = -1;
			double maxScore = -1;
			
			// for each unselected document
			for (int j = 0; j < n; j++) {
				// skip already selected documents
				if (selected.contains(docids[j])) {
					continue;
				}
				
				double diversity = 0;
				
				// for each sub-query
				for (int i = 0; i < k; i++) {
					diversity += importance[i] * coverage[j][i] * novelty[i];
				}
				
				double score = (1-ambiguity) * relevance[j] + ambiguity * diversity;
				
				if (score > maxScore) {
					maxRank = j;
					maxScore = score;
				}
			}
			

			// update the score of the selected document
			if (maxRank == -1) {
				break;
			}
			
			scores[maxRank] = maxScore;
			
			// mark as selected
			selected.add(docids[maxRank]);
			currentSelected.put(docids[maxRank], maxScore);
			System.out.println("Selected: " + maxRank + " " + maxScore);
			for (int i = 0; i < coverage[maxRank].length; i++) {
				System.out.print(coverage[maxRank][i] + " ");
			}
			System.out.println();
			double diversity = 0;
			
			// for each sub-query
			for (int i = 0; i < k; i++) {
				diversity += importance[i] * coverage[maxRank][i] * novelty[i];
			}
			
			System.out.println("diversity " + diversity);
			// update novelty estimations
			
			for (int i = 0; i < k; i++) {
				novelty[i] *= Math.pow(0.9, (double) 1 - coverage[maxRank][i]);
			}

			
		}
		
		for (int i = 0; i < k; i++) {
		//	System.out.println(novelty[i]);
		}
		//System.out.println();
		
		return scores;
	}
}
