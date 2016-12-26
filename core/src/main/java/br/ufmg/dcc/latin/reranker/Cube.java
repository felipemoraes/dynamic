package br.ufmg.dcc.latin.reranker;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.ResultSet;

public class Cube extends InteractiveReranker {
	
	public Cube(FeedbackModeling feedbackModeling) {
		super(feedbackModeling);
	}

	double gamma;
	private static double MaxHeight = 5.0f;
	private double[] importance;
	private double[] novelty;
	private double[][] coverage;
	private double[] accumulatedGain;

	
	private FeedbackModeling feedbackModeling;
	
	

	@Override
	public void start(ResultSet resultSet, double[] params){
		super.start(resultSet, params);
		gamma = params[0];
		coverage = feedbackModeling.coverage;
		importance = feedbackModeling.importance;
		novelty = feedbackModeling.novelty;
		accumulatedGain = feedbackModeling.accumulatedGain;
	}
	
	private float discountFactor(int subtopic){
		
		return (float) Math.pow(gamma, novelty[subtopic]);
	}
	
	@Override
	protected double score(int docid) {
		if (importance.length == 0) {
			return relevance[docid];
		}
		double score = 0;
		for (int i = 0; i < importance.length; i++) {
			
			if (accumulatedGain[i] < MaxHeight) {
				score += discountFactor(i)*importance[i]*coverage[docid][i]*accumulatedGain[i];
			}
		}
		return score;
	}

	@Override
	protected void update(int docid) {
		for (int i = 0; i < importance.length; i++) {
			accumulatedGain[i] += coverage[docid][i];
		}

	}

	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		
		feedbackModeling.update(feedback);
		coverage = feedbackModeling.coverage;
		importance = feedbackModeling.importance;
		novelty = feedbackModeling.novelty;
		accumulatedGain = feedbackModeling.accumulatedGain;
	}

}
