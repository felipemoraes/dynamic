package br.ufmg.dcc.latin.models;


import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;


public class xQuAD {

	private double[][] novelty;
	private HashSet<Integer> selected;
	private HashMap<Integer,Double> currentSelected;
	
	public xQuAD(){
		
		selected = new HashSet<Integer>();
		currentSelected = new HashMap<Integer,Double>();
		
	}
	
	
	public void updateNovelty(int[] docids, double[][][] coverage){
		
		
		novelty = new double[coverage.length][];
		for (int m = 0; m < novelty.length ; ++m) {
			if (coverage[m] == null) {
				continue;
			}
			novelty[m] = new double[coverage[m][0].length];
			Arrays.fill(novelty[m], 1);
			for (int j = 0; j < docids.length; j++) {
				if (selected.contains(docids[j])) {
					for (int i = 0; i < coverage[m][0].length; i++) {
						novelty[m][i] *=  (1-coverage[m][j][i]/docids.length);
					}
					
				}
			}
		}

		
	}
	
	public double[] score(int[] docids, double[] relevance,
			double[][][] coverage, double[][] importance, double[] weight, double ambiguity, int depth) {
		
		int n = docids.length;
		
		
		// select more 5 documents
		int s = selected.size() + 5;
		if (s > n) {
			s = n;
		}
		
		if (depth > (n - selected.size())){ 
			depth = n - selected.size();
		}
		
		double[] scores = new double[n];
		Arrays.fill(scores, 0);
		
		// greedily diversify the all documents
		while (selected.size() < s) {
			int maxRank = -1;
			double maxScore = -1;
			double ss = 0;
			boolean flag = false;
			// for each unselected document
			int d = 0;
			for (int j = 0; j < n; ++j){
				
				// skip already selected documents
				if (selected.contains(docids[j])) {
					continue;
				}
				
				if ( d >= depth) {
					break;
				}
				
				
				double diversity = 0;
				
				// for each sub-query
				int f = 0;
				if (importance != null) {
					f = importance.length;
				}
				for (int m = 0; m < f; ++m) {
					int k = 0; 
					if (importance[m] != null) {
						k = importance[m].length;
					}
					
					for (int i = 0; i < k; i++) {
						diversity += weight[m]*importance[m][i] * coverage[m][j][i] * novelty[m][i];
					}
				}
				
				double score = (1-ambiguity) * relevance[j] + ambiguity * diversity;
				ss = score;
				if (score > maxScore) {
					maxRank = j;
					maxScore = score;
				}
				
				
				d++;
			}
			
			if (maxRank == -1){
				break;
			}
			// update the score of the selected document
			scores[maxRank] = maxScore;
			
			// mark as selected
			selected.add(docids[maxRank]);
			currentSelected.put(docids[maxRank], maxScore);
			int f = 0;
			if (importance != null) {
				f = importance.length;
			}
			
			for (int m = 0; m < f; ++m){
				int k = 0; 
				if (importance[m] != null) {
					k = importance[m].length;
				}
				for (int i = 0; i < k; i++) {
					novelty[m][i] *=  (double) 1 - coverage[m][maxRank][i]/n;
				}
			}

			
		}
		
		return scores;
	}


	public HashSet<Integer> getSelected() {
		return selected;
	}


	public void setSelected(HashSet<Integer> selected) {
		this.selected = selected;
	}


	public HashMap<Integer,Double> getCurrentSelected() {
		return currentSelected;
	}


	public void setCurrentSelected(HashMap<Integer,Double> currentSelected) {
		this.currentSelected = currentSelected;
	}


}
