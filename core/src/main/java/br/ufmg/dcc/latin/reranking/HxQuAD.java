package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;

public class HxQuAD extends InteractiveReranker {

	private double[][] novelty;
	private double[] noveltyBottomUp;
	
	private double[][] importance;
	private double[][][] coverage;
	
	public double alpha;
	public double lambda;
	
	private String aspectMiningClassName;
	
	private AspectMining aspectMining;
	
	public HxQuAD(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	@Override
	public ResultSet get(){
		aspectMining.updateHierarchicalAspects(indexName);
		coverage = aspectMining.getHierarchicalCoverage();
		importance = aspectMining.getHierarchicalImportance();
		novelty = aspectMining.getHierarchicalNovelty();
		updateNovelty();
		return super.get();
	}

	private void updateNovelty() {
		for (int j = 0; j < docids.length; ++j) {
			if (! selected.has(j)) {
				continue;
			}
			update(j);
		}
		
	}

	@Override
	public void start(double[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		noveltyBottomUp = new double[20];
		Arrays.fill(noveltyBottomUp, 1.0d);
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		double[] aspectWeights = new double[9];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		aspectMining.setAspectWeights(aspectWeights);
		coverage = aspectMining.getHierarchicalCoverage();
		importance = aspectMining.getHierarchicalImportance();
		novelty = aspectMining.getHierarchicalNovelty();
	}

	@Override
	public void setParams(double[] params){
		super.start(params);
		lambda = params[1];
		alpha = 0.5;
		noveltyBottomUp = new double[20];
		Arrays.fill(noveltyBottomUp, 1.0d);
		double[] aspectWeights = new double[9];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		aspectMining.setAspectWeights(aspectWeights);
	}
	
	@Override
	protected double score(int docid) {
		double[] diversity = {0.0,0.0};
		
		for (int i = 0; i < importance.length; i++) {
			double coverageBottomUp = 1;
			double importanceBottomUp = 1/importance.length;
			for (int j = 0; j < importance[i].length; j++) {
				coverageBottomUp *= (1-coverage[docid][i][j]);
				diversity[1] +=  importance[i][j]*coverage[docid][i][j]*novelty[i][j];
			}
			coverageBottomUp = 1 - coverageBottomUp;
			diversity[0] += coverageBottomUp*importanceBottomUp*noveltyBottomUp[i];
			
		}

		double score = (1-lambda) * relevance[docid] 
				+ lambda * ( alpha*diversity[0] + (1-alpha)*diversity[1]);
		
		return score;
	}

	@Override
	protected void update(int docid) {
		
		for (int i = 0; i < coverage[docid].length; i++) {
			double coverageBottomUp = 1;
			for (int j = 0; j < coverage[docid][i].length; j++) {
				novelty[i][j] *=  (1 - coverage[docid][i][j]);
				coverageBottomUp *= (1-coverage[docid][i][j]);
			}
			coverageBottomUp = 1-coverageBottomUp;
			
			noveltyBottomUp[i] *= (1-coverageBottomUp);
			
		}


	}

	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		aspectMining.sendFeedback(indexName, query,feedback);
	}
	
	@Override
	public String debug() {
		// TODO Auto-generated method stub
		return null;
	}

}
