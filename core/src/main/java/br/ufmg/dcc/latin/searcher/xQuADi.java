package br.ufmg.dcc.latin.searcher;


import java.util.Arrays;
import java.util.HashSet;

public class xQuADi {

	private double[] novelty;
	private HashSet<Integer> selected;
	
	public xQuADi(){
		novelty = new double[100];
		selected = new HashSet<Integer>();
		Arrays.fill(novelty, -1);
	}
	
	
	public double[] score(int[] docids, double[] relevance,
			double[][] coverage, double[] importance, double ambiguity, int depth, int k) {
		
		
		int n = docids.length;
		
		// depth size now is updated
		depth += selected.size();
		
		if (k == 0) {
			while(selected.size() < depth){
				for (int i = 0; i < n; i++) {
					if (!selected.contains(docids[i])) {
						selected.add(docids[i]);
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
					novelty[j] *= (double) 1 - coverage[i][j];
					
				}
			}
		}
		for (int j = 0; j < k; j++) {
			System.out.println(novelty[j]);
			
		}
		


		double[] scores = new double[n];
		Arrays.fill(scores, 0);
		
		
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
				System.out.print(docids[j] + ":" + score + " ");
				if (score > maxScore) {
					maxRank = j;
					maxScore = score;
				}
			}
			System.out.println();
			System.out.println(docids[maxRank] + " " + maxScore);
			// update the score of the selected document
			scores[maxRank] = maxScore;
			
			// mark as selected
			selected.add(docids[maxRank]);
			
			// update novelty estimations
			for (int i = 0; i < k; i++) {
				novelty[i] *= (double) 1 - coverage[maxRank][i];
			}
		}
		
		
		return scores;
	}
}
