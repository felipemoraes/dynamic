package br.ufmg.dcc.latin.reranking;


import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xQuAD3 extends InteractiveReranker {

	double lambda;
	
	private AspectMining aspectMining;
	
	public double[] importance;
	public double[][] novelty;
	public double[][] coverage;
	public double[][][] features;
	
	int n;
	
	private String aspectMiningClassName;
	
	public xQuAD3(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	
	@Override
	public double score(int docid){
		double diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i]*novelty[i][docid];
		}
		
		double score = (1-lambda)*relevance[docid] + lambda*diversity;
		
		return score;
	}
	
	@Override
	public void start(double[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		n = relevance.length;
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = new double[0][n];
		features = new double[n][0][0];
	}

	@Override
	protected void update(int docid) {
		for (int i = 0; i < novelty.length; i++) {
			double[] probs = new double[n];
			
			for (int j = 0; j < probs.length; j++) {
				probs[j] = cosine(features[docid][i], features[j][i]);
			}
			probs = normalize(probs);
			for (int j = 0; j < probs.length; j++) {
				novelty[i][j] *= (1-probs[j]);
			}
		}
		
	}

	@Override
	public void update(Feedback[] feedback) {
		
		aspectMining.miningFeedback(indexName, query,feedback);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		features = aspectMining.getFeatures();
		int aspectSize = importance.length;
		novelty = new double[aspectSize][n];
		for (int i = 0; i < novelty.length; i++) {
			Arrays.fill(novelty[i], 1f);
		}
		updateNovelty();
	
	}
	public void updateNovelty(){
		for (int j = 0; j < docids.length; ++j) {
			if (! selected.has(docids[j])) {
				continue;
			}
			update(j);
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
	public String debug(String topicid, int iteration) {
		return "";
	}


}
