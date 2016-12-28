package br.ufmg.dcc.latin.reranker;

import java.util.Arrays;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.ResultSet;

public class xMMR extends InteractiveReranker {

	public xMMR(FeedbackModeling feedbackModeling) {
		super(feedbackModeling);
	}

	public double[] cacheSim;
	
	public int n;

	public double lambda;
	
	public double[][] coverage; 



	@Override
	public void start(ResultSet resultSet, double[] params) {
		super.start(resultSet, params);
		lambda = params[0];
		n = relevance.length;
		cacheSim = new double[n];
		relevance = normalize(relevance);
		feedbackModeling = feedbackModeling.getInstance(docnos);
		coverage = feedbackModeling.coverage;
	}

	@Override
	public double score(int docid) {
		
		double score = lambda*(relevance[docid]) - (1-lambda)*cacheSim[docid];
		return score;
	}


	@Override
	public void update(int docid) {
		
		double[] newCache = new double[n];
	    Arrays.fill(newCache, 0);
	    
	    for(int i = 0;i<newCache.length;++i) {
	    	
	    	newCache[i] = cosine(coverage[i],coverage[docid]);
	    }
	    
	    
	    newCache = normalize(newCache);
	    
	    for (int i = 0; i < newCache.length; i++) {
	    	
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}
	  
		
	}
	
	private double cosine(double[] v1, double[] v2){
		double denom = 0;
		double sum1 = 0;
		double sum2  = 0;
		
	
		for (int i = 0; i < v2.length; i++) {
			denom += v1[i]*v2[i];
		}
		
		for (int i = 0; i < v2.length; i++) {
			sum1 += v1[i]*v1[i];
			sum2 += v2[i]*v2[i];
		}
		sum1 = (double) Math.sqrt(sum1);
		sum2 = (double) Math.sqrt(sum2);
		
		if (sum1*sum2 > 0){
			return denom/(sum1*sum2);
		} 
		
		return 0;
	}

	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		feedbackModeling.update(feedback);
		coverage = feedbackModeling.coverage;
		
	}
	

	
}
