package br.ufmg.dcc.latin.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class HxQuAD {
	private double[][][] novelty;
	private double[][] noveltyBottomUp;
	private HashSet<Integer> selected;
	private HashMap<Integer,Double> currentSelected;
	
	public HxQuAD(){
		selected = new HashSet<Integer>();
		currentSelected = new HashMap<Integer,Double>();
	}
	

	public void updateNovelty(int[] docids, double[][][][] coverage){
		int f = coverage.length;
		int n = docids.length;
		noveltyBottomUp = new double[f][];
		novelty = new double[f][][];
		
		for (int m = 0; m < f ; ++m) {
			if (coverage[m] == null) {
				continue;
			}
			int k = coverage[m][0].length;
			noveltyBottomUp[m] = new double[k];
			novelty[m] = new double[k][];
			Arrays.fill(noveltyBottomUp[m], 1);
			for (int i = 0; i < k; ++i){
				int l = coverage[m][0][i].length;
				novelty[m][i] = new double[l];
				Arrays.fill(novelty[m][i], 1);
			}
		}
		
		for (int m = 0; m < f ; ++m) {
			if (coverage[m] == null) {
				continue;
			}
			for (int j = 0; j < docids.length; j++) {
				if (selected.contains(docids[j])) {
					int k = coverage[m][0].length;
					for (int i = 0; i < k; i++) {
						double coverageBottomUp = 1;
						for (int l = 0; l < coverage[m][j][i].length; l++) {
							
							novelty[m][i][l] *=  (1 - coverage[m][j][i][l]/n);
							coverageBottomUp *= ( 1-  coverage[m][j][i][l]);
						}
						coverageBottomUp = 1 - coverageBottomUp;
						noveltyBottomUp[m][i] *=  (1-coverageBottomUp/n);
					}
					
				}
			}
		}
		
	}
	
	
	public double[] score(int[] docids, double[] relevance,
			double[][][][] coverage, double[][][] importance, double[] weight, double[] ambiguity,  int depth) {
		
		
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
			int d = 0;
			// for each unselected document
			for (int j = 0; j < n; ++j){
				// skip already selected documents
				
				if ( d >= depth) {
					break;
				}
				
				if (selected.contains(docids[j])) {
					continue;
				}
				
				double[] diversity = {0.0,0.0};
				
				// for each sub-query
				
				if (coverage != null) {
					int f = coverage.length;
					// first level
					for (int m = 0 ; m < f; ++m) {
						if (coverage[m] == null) {
							continue;
						}
						for (int i = 0; i < coverage[m][j].length ; i++) {
							double coverageBottomUp = 1;
							double importanceBottomUp = 1;
							for (int k = 0; k < coverage[m][j][i].length; k++) {
								coverageBottomUp *= (1-coverage[m][j][i][k]);
							}
							coverageBottomUp = 1 - coverageBottomUp;
							diversity[1] += weight[m]*importanceBottomUp * coverageBottomUp * noveltyBottomUp[m][i];
							
						} 
					}
					
					// second level
					for (int m = 0; m < f; ++m){
						if (coverage[m] == null) {
							continue;
						}
						for (int i = 0; i < coverage[m][j].length ; i++) {
							for (int k = 0; k < coverage[m][j][i].length; k++) {
								diversity[1] += weight[m]*importance[m][i][k] * coverage[m][j][i][k] * novelty[m][i][k];
							}
							
						}
					}
				}
				double score = (1-ambiguity[0]) * relevance[j] 
						+ ambiguity[0] * ( (1-ambiguity[1])*diversity[0] + ambiguity[1]*diversity[1]);
				
				if (score > maxScore) {
					maxRank = j;
					maxScore = score;
				}
				
				d++;
			}
			

			// update the score of the selected document
			if (maxRank == -1) {
				break;
			}
			scores[maxRank] = maxScore;
			
			// mark as selected
			selected.add(docids[maxRank]);
			currentSelected.put(docids[maxRank], maxScore);
			if (coverage != null) {
				// update novelty second level
				int f = coverage.length;
				for (int m = 0; m < f ; ++m) {
					if (coverage[m] == null) {
						continue;
					}
					for (int i = 0; i < coverage[m][maxRank].length; i++) {
						for (int k = 0; k < coverage[m][maxRank][i].length; k++) {
							novelty[m][i][k] *=  1 - coverage[m][maxRank][i][k];
						}
					}
					
					
					// update novelty first level
					for (int i = 0; i < coverage[m][maxRank].length ; i++) {
						double coverageBottomUp = 1;	
						for (int k = 0; k < coverage[m][maxRank][i].length; k++) {
							coverageBottomUp *= (1-coverage[m][maxRank][i][k]);
						}
						coverageBottomUp = 1 - coverageBottomUp;
						noveltyBottomUp[m][i] *=  (1-coverageBottomUp);
					}
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
