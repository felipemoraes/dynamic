package br.ufmg.dcc.latin.reranker;

import java.util.Arrays;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.ResultSet;

public class PM2 extends InteractiveReranker {

	public PM2(FeedbackModeling feedbackModeling) {
		super(feedbackModeling);
	}


	public double lambda;
	public int[] highestAspect;
	
	public double[] v;
	public double[] s;
	public double[][] coverage;

	
	public int highestAspect(){
		
		int maxQ =  -1;
		double maxQuotient = -1;
		for (int i = 0; i < v.length; i++) {
			double quotient = v[i]/(2*s[i]+1);
			if (quotient > maxQuotient) {
				maxQ = i;
				maxQuotient = quotient;
			}
		}
		
		return maxQ;
	}
	
	public double score(int docid){
		
		int q = highestAspect();
		
		if (q == -1){
			return relevance[docid];
		}
		
		double quotientAspectq = v[q]/(2*s[q]+1);
		quotientAspectq *= coverage[docid][q];
		double quotientotherAspect  = 0;
		for (int i = 0; i < s.length; i++) {
			if (i != q) {
				quotientotherAspect += (v[i]/(2*s[i]+1))*coverage[docid][i];
			}
		}
		double score = lambda*quotientAspectq + (1-lambda)*quotientotherAspect;
		
		return score;
	}
	
	public void update(int docid){
		int q = highestAspect[docid];
		if (q == -1) {
			q = highestAspect();
		} 
		
		highestAspect[docid] = q;
		
		if (q == -1) {
			return;
		}
		
		double allCoverage = 0;
		
		for (int i = 0; i < coverage[docid].length; ++i) {
			allCoverage += coverage[docid][i];
		}
		
		if (allCoverage > 0) {
			s[q] += coverage[docid][q]/allCoverage;
		} 
		
	}
	
	public void updateNovelty(){
		for (int j = 0; j < docids.length; ++j) {
			if (! selected.has(j)) {
				continue;
			}
			update(j);
		}
	}
	
	@Override
	public void start(ResultSet resultSet, double[] params){
		super.start(resultSet,params);
		relevance = normalize(relevance);
		feedbackModeling = feedbackModeling.getInstance(docnos);
		coverage = feedbackModeling.coverage;
		v = feedbackModeling.v;
		s = feedbackModeling.s;
		lambda = params[0];
		int n = relevance.length;
		highestAspect = new int[n];
		
		Arrays.fill(highestAspect, -1);
		
	}


	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		feedbackModeling.update(feedback);
		coverage = feedbackModeling.coverage;
		v = feedbackModeling.v;
		s = feedbackModeling.s;
		updateNovelty();
		
	}
	
	@Override
	public void updateDropAspect(Feedback[] feedback, double frac) {
		feedbackModeling.updateDropAspect(feedback,frac);
		coverage = feedbackModeling.coverage;
		v = feedbackModeling.v;
		s = feedbackModeling.s;
		updateNovelty();
		
	}
	
	
	
	@Override
	public void updateDropFeedback(Feedback[] feedback, double frac) {
		feedback = removeFeedback(feedback,frac);
		feedbackModeling.update(feedback);
		coverage = feedbackModeling.coverage;
		v = feedbackModeling.v;
		s = feedbackModeling.s;
		updateNovelty();
		
	}

}
