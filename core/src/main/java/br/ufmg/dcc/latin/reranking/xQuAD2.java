package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xQuAD2 extends InteractiveReranker {
	
	double lambda;
	
	
	
	private double[] importance;
	private double[] novelty;
	private double[][] coverage;
	
	int n;
	private String aspectMiningClassName;
	private AspectMining aspectMining;
	
	public xQuAD2(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	@Override
	public void start(double[] params){
		super.start(params);
		relevance = normalize(relevance);
		n = relevance.length;
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		lambda = params[1];
		novelty = new double[relevance.length];
		Arrays.fill(novelty, 1.0f);
	}
	

	@Override
	protected double score(int docid) {
		double diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i];
		}
		
		
		double score = (1-lambda)*relevance[docid] + lambda*diversity*novelty[docid];
		return score;
	}


	@Override
	public void update(int docid) {
		
		double[] probs = new double[n];
	    Arrays.fill(probs, 0);

	    for(int i = 0;i<probs.length;++i) {
	    	probs[i] = cosine(coverage[i],coverage[docid]);
	    }
	    
	    probs = normalize(probs);
	    
	    for (int i = 0; i < probs.length; i++) {
	    	 novelty[i] *= (1-probs[i]);
	    	 
	    	
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
		aspectMining.sendFeedback(indexName, query,feedback);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = new double[n];
		Arrays.fill(novelty, 1f);
		updateNovelty();
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
	public String debug(String topicid, int iteration) {
		return "";
	}

}
