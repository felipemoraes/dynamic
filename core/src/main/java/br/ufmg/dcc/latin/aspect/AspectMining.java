package br.ufmg.dcc.latin.aspect;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.feedback.Feedback;

public abstract class AspectMining {
	
	protected Feedback[] feedbacks;

	protected int n;
	
	protected float[] importance;
	protected float[] novelty;
	protected float[][] coverage;
	
	protected float[] accumulatedRelevance;
	
	protected float[][][] features;
	
	protected float[] v;
	protected float[] s;
	
	
	public abstract void miningFeedback(String index, String query, Feedback[] feedbacks);
	public abstract void miningFeedbackForCube(String index,  String query, Feedback[] feedbacks);
	
	public float[][] getCoverage(){
		return coverage;
	}
	
	public float[] getImportance(){
		return importance;
	}
	
	public float[] getNovelty(){
		return novelty; 
	}
	
	protected void cacheFeedback(Feedback[] feedbacks){
		
		if (this.feedbacks == null) {
			this.feedbacks = new Feedback[n];
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < feedbacks.length; j++) {
				if (feedbacks[j].getDocno().equals(RetrievalCache.docnos[i])){
					this.feedbacks[i] = feedbacks[j]; 
				}
			}
		}
	}
	

	
	protected void normalizeCoverage(){
		for (int i = 0; i < coverage[0].length; ++i) {
			float sum = 0;
			for (int j = 0; j < coverage.length; j++) {
				sum += coverage[j][i];
			}
			
			for (int j = 0; j < coverage.length; j++) {
				if (sum > 0) {
					float normValue = coverage[j][i]/sum;
					coverage[j][i] = normValue;
				}
				
			}
		}
	}

	protected void printCoverage() {
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[i].length; j++) {
				System.out.print(coverage[i][j] + " ");
			}
		}
		
	}
	
	public void printNovelty() {
		for (int i = 0; i < novelty.length; i++) {
			System.out.print(novelty[i] + " ");
		}
		System.out.println();
	}

	protected float[] scaling(float[] scores){
		float min = Float.POSITIVE_INFINITY;
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] < min) {
				min = scores[i];
			}
		}
		
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > max) {
				max = scores[i];
			}
		}
		
		for (int i = 0; i < scores.length; i++) {
			if (max!=min) {
				scores[i] = (scores[i]-min)/(max-min);
			} else {
				scores[i] = 0;
			}
			
		}
		return scores;
		
	}
	
	public float[] getV() {
		return v;
	}

	public float[] getS() {
		return s;
	}
	
	public float[] getAccumulatedRelevance(){
		return accumulatedRelevance;
	}
	
	public float[][][] getFeatures(){
		return features;
	}
}
