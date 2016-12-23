package br.ufmg.dcc.latin.feedback.modeling;

import br.ufmg.dcc.latin.feedback.Feedback;

public abstract class FeedbackModeling {
	
	protected Feedback[] feedbacks;

	protected int n;
	
	protected double[] importance;
	protected double[] novelty;
	protected double[][] coverage;
	
	protected double[] accumulatedRelevance;
	
	protected double[][][] features;
	
	protected double[] v;
	protected double[] s;
	
	protected double[] aspectWeights;
	
	
	public abstract void sendFeedback(String index, String query, Feedback[] feedbacks);
	public abstract void updateAspects(String index);
	public void debug(){
		
	}
	public abstract void miningFeedbackForCube(String index,  String query, Feedback[] feedbacks);
	
	public double[][] getCoverage(){
		return coverage;
	}
	
	public double[] getImportance(){
		return importance;
	}
	
	public double[] getNovelty(){
		return novelty; 
	}
	
	protected void cacheFeedback(Feedback[] feedbacks){
		
		if (this.feedbacks == null) {
			this.feedbacks = new Feedback[n];
		}
		
		for (int j = 0; j < feedbacks.length; j++) {
			int i = feedbacks[j].index;
			this.feedbacks[i] = feedbacks[j]; 
		}
	}
	

	public double[] getAspectWeights() {
		return aspectWeights;
	}

	public void setAspectWeights(double[] aspectWeights) {
		this.aspectWeights = aspectWeights;
	}

	
	protected void normalizeCoverage(){
		for (int i = 0; i < coverage[0].length; ++i) {
			float sum = 0;
			for (int j = 0; j < coverage.length; j++) {
				sum += coverage[j][i];
			}
			
			for (int j = 0; j < coverage.length; j++) {
				if (sum > 0) {
					double normValue = coverage[j][i]/sum;
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

	protected double[] scaling(double[] scores){
		double min = Double.POSITIVE_INFINITY;
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] < min) {
				min = scores[i];
			}
		}
		double max = Double.NEGATIVE_INFINITY;
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
	
	public double[] getV() {
		return v;
	}

	public double[] getS() {
		return s;
	}
	
	public double[] getAccumulatedRelevance(){
		return accumulatedRelevance;
	}
	
	public double[][][] getFeatures(){
		return features;
	}
}
