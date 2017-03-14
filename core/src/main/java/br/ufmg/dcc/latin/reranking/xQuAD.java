package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;

public class xQuAD extends InteractiveReranker {

	double lambda;
	
	private AspectMining aspectMining;
	
	public double[] importance;
	public double[] novelty;
	public double[][] coverage;

	
	private String aspectMiningClassName;
	
	public xQuAD(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	

	@Override
	public double score(int docid){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i]*novelty[i];
		}
		double score = (1-lambda)*relevance[docid] + lambda*diversity;
		
		return score;
	}
	
	@Override
	public ResultSet get(){
		aspectMining.updateAspects(indexName);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
		updateNovelty();
		return super.get();
	}
	
	@Override
	public void start(double[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		double[] aspectWeights = new double[10];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		aspectMining.setAspectWeights(aspectWeights);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
	}
	
	
	@Override
	public void setParams(double[] params){
		super.start(params);
		lambda = params[1];
		
		double[] aspectWeights = new double[10];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		aspectMining.setAspectWeights(aspectWeights);
	}
	
	@Override
	public void update(int docid){
		for (int i = 0; i < novelty.length; i++) {
			novelty[i] *= (1-coverage[docid][i]);
		}
		
	}

	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		aspectMining.sendFeedback(indexName, query,feedback);
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
	public String debug() {
		aspectMining.debug();
		return null;
	}

}
