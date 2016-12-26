package br.ufmg.dcc.latin.feedback.modeling;

import java.util.Arrays;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.user.TrecUser;

public class FeedbackModeling {
	
	protected Feedback[] feedbacks;

	protected int n;
	
	public double[] importance;
	
	public double[] novelty;
	
	public double[][] coverage;
	
	public double[] accumulatedGain;
	
	public double[] v;
	public double[] s;
	
	public String[] docnos;
	
	private FeedbackModel feedbackModel;
	
	public TrecUser trecUser;
	
	public FeedbackModeling(){
	}
	
	public FeedbackModeling getInstance(String[] docnos){
		
		n = docnos.length;
		this.docnos = docnos;
		importance = new double[0];
		novelty = new double[0];
		accumulatedGain = new double[0];
		coverage = new double[n][0];
		v =  new double[0];
		s =  new double[0];
		feedbackModel = new FeedbackModel();
		return this;
	}
	
	
	public void update(Feedback[] feedbacks){
		
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].onTopic){
				continue;
			}
			for (int j = 0; j < feedbacks[i].passages.length; j++) {
				feedbackModel.addSubtopic(feedbacks[i].passages[j].subtopicId);
			}
		}
		
		int numberOfSubtopics = feedbackModel.size;
		importance = new double[numberOfSubtopics];
		novelty = new double[numberOfSubtopics];
		coverage = new double[n][numberOfSubtopics];
		
		Arrays.fill(novelty, 1);
		Arrays.fill(importance, 1f/numberOfSubtopics);
		
		for (int i = 0; i < numberOfSubtopics; i++) {
			String subtopicId = feedbackModel.getSubtopicId(i);
			double[] relevances = trecUser.get(subtopicId,docnos);
			for (int j = 0; j < relevances.length; j++) {
				coverage[j][i] = relevances[j];
			}
		}
		normalizeCoverage();
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
			if ( i < 10) {
			for (int j = 0; j < coverage[i].length; j++) {
				System.out.print(coverage[i][j] + " ");
			}
			System.out.println();
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
	
}
