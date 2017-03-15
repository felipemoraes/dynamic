package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;

public class PM2 extends InteractiveReranker {

	double lambda;
	int[] highestAspect;
	
	private double[] v;
	private double[] s;
	private double[][] coverage;

	private String aspectMiningClassName;
	
	AspectMining aspectMining;

	public PM2(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	@Override
	public ResultSet get(){
		aspectMining.updateAspects(indexName);
		coverage = aspectMining.getCoverage();
		v = aspectMining.getV();
		s = aspectMining.getS();
		updateNovelty();
		return super.get();
	}
	
	
	@Override
	public void start(double[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		int n = relevance.length;
		
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		double[] aspectWeights = new double[9];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		aspectMining.setAspectWeights(aspectWeights);
		coverage = aspectMining.getCoverage();
		v = aspectMining.getV();
		s = aspectMining.getS();
		
		highestAspect = new int[n];
		Arrays.fill(highestAspect, -1);
		
	}
	
	@Override
	public void setParams(double[] params){
		super.start(params);
		lambda = params[1];
		
		double[] aspectWeights = new double[9];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		aspectMining.setAspectWeights(aspectWeights);
	}
	
	
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
		double allCoverage = 0;
		for (int i = 0; i < coverage[docid].length; ++i) {
			allCoverage += coverage[docid][i];
		}
		if (allCoverage > 0) {
			double newS = s[q] + coverage[docid][q]/allCoverage;
			s[q] = newS;
		} 
		highestAspect[docid] = q;
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
	public void update(Feedback[] feedback) {
		super.update(feedback);
		aspectMining.sendFeedback(indexName, query,feedback);
	}


	@Override
	public String debug() {
		return "";
	}

}
