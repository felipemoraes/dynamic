package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;

public class HPM2 extends InteractiveReranker {

	private double[] v;
	private double[][] s;
	private double[] sBottomUp;
	private double[][][] coverage;

	int[] highestAspectFirst;
	int[][] highestAspectSecond;
	
	public double alpha;
	public double lambda;
	
	
	private String aspectMiningClassName;

	public HPM2(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	
	private AspectMining aspectMining;
	
	@Override
	public ResultSet get(){
		aspectMining.updateHierarchicalAspects(indexName);
		coverage = aspectMining.getHierarchicalCoverage();
		s = aspectMining.getHierarchicalS();
		v = aspectMining.getV();
		updateNovelty();
		return super.get();
	}
	
	public int highestFirstAspect(){
		int maxQ =  -1;
		double maxQuotient = -1;
		for (int i = 0; i < v.length; i++) {
			double quotient = v[i]/(2*sBottomUp[i]+1);
			if (quotient > maxQuotient) {
				maxQ = i;
				maxQuotient = quotient;
			}
		}
		
		return maxQ;
	}
	
	public int[] highestSecondAspect(){
		int[] maxQ =  {-1,-1};
		double maxQuotient = -1;
		for (int i = 0; i < s.length; i++) {
			for (int j = 0; j < s[i].length; j++) {
				double quotient = 1/(2*s[i][j]+1);
				if (quotient > maxQuotient) {
					maxQ[0] = i;
					maxQ[1] = j;
					maxQuotient = quotient;
				}
			}

		}
		
		return maxQ;
	}
	
	@Override
	protected double score(int docid) {
		double[] diversity = {0.0,0.0};
		
		// Select best first level
		int q = highestFirstAspect();
		if (q == -1) {
			return relevance[docid];
		}
		
		double quotientAspectq = v[q]/(2*sBottomUp[q]+1);
		double coverageBottomUp = 1;
		for (int j = 0; j < s[q].length; j++) {
			coverageBottomUp *= (1-coverage[docid][q][j]);
			
		}
		coverageBottomUp = 1 - coverageBottomUp;
		quotientAspectq *= coverageBottomUp;
		double quotientotherAspect  = 0;
		for (int i = 0; i < s.length; i++) {
			if (i != q) {
				coverageBottomUp = 1;
				for (int j = 0; j < s[i].length; j++) {
					coverageBottomUp *= (1-coverage[docid][i][j]);
					
				}
				coverageBottomUp = 1 - coverageBottomUp;
				quotientotherAspect += (v[i]/(2*sBottomUp[i]+1))*coverageBottomUp;

			}
		}
		diversity[0] = lambda*quotientAspectq + (1-lambda)*quotientotherAspect;
		
		int[] qs = highestSecondAspect();
		if (qs[0] == -1) {
			return relevance[docid];
		}
		
			
		quotientAspectq = 1/(2*s[qs[0]][qs[1]]+1);

		quotientAspectq *=  coverage[docid][qs[0]][qs[1]];
		quotientotherAspect  = 0;
		for (int j = 0; j < s.length; j++) {
			for (int k = 0; k < s[j].length; k++) {
				if (j!= qs[0] && k != qs[1]) {
					quotientotherAspect += (1/(2*s[j][k]+1))*coverage[docid][j][k];

				}
			}
		}

		
		diversity[1] = lambda*quotientAspectq + (1-lambda)*quotientotherAspect;
		
		double score =  alpha*diversity[0] + (1-alpha)*diversity[1];
		return score;
	}

	@Override
	protected void update(int docid) {
		int q = highestAspectFirst[docid];
		int[] qs = null;
 		if (q == -1) {
			q = highestFirstAspect();
		} 
 		
 		if (highestAspectSecond[docid] == null) {
 			qs = highestSecondAspect();
 		} else {
 			qs = highestAspectSecond[docid];
 		}
 		
 		double allCoverage = 0;
		for (int i = 0; i < coverage[docid].length; ++i) {
			for (int j = 0; j <  coverage[docid][i].length; j++) {
				allCoverage += coverage[docid][i][j];
			}
		}
		
		
		
		if (allCoverage > 0) {
			double newS = s[qs[0]][qs[1]] + coverage[docid][qs[0]][qs[1]]/allCoverage;
			s[qs[0]][qs[1]] = newS;
		} 
		
		
 		double allCoverageBottomUp = 0;
		for (int i = 0; i < coverage[docid].length; ++i) {
			
			double coverageBottomUp = 1;
			for (int j = 0; j < s[q].length; j++) {
				coverageBottomUp *= (1-coverage[docid][q][j]);
				
			}
			coverageBottomUp = 1 - coverageBottomUp;
			allCoverageBottomUp += coverageBottomUp;
		}
		
		
		
		if (allCoverageBottomUp > 0) {
			double coverageBottomUp = 0;
			for (int j = 0; j < s[q].length; j++) {
				coverageBottomUp *= (1-coverage[docid][q][j]);
				
			}
			coverageBottomUp = 1 - coverageBottomUp;
			double newS = sBottomUp[q] + coverageBottomUp/allCoverageBottomUp;
			sBottomUp[q] = newS;
		} 
		
		highestAspectFirst[docid] = q;
		highestAspectSecond[docid] = qs;
	}
	
	@Override
	public void start(double[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		sBottomUp = new double[20];
		Arrays.fill(sBottomUp, 1.0d);
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		double[] aspectWeights = new double[9];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		aspectMining.setAspectWeights(aspectWeights);
		coverage = aspectMining.getHierarchicalCoverage();
		v = aspectMining.getV();
		s = aspectMining.getHierarchicalS();
		
		int n = relevance.length;
		
		highestAspectFirst = new int[n];
		Arrays.fill(highestAspectFirst, -1);
		highestAspectSecond = new int[n][2];
	}

	@Override
	public void setParams(double[] params){
		super.start(params);
		lambda = params[1];
		alpha = 0.5;
		sBottomUp = new double[20];
		Arrays.fill(sBottomUp, 1.0d);
		double[] aspectWeights = new double[9];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		int n = relevance.length;
		highestAspectFirst = new int[n];
		Arrays.fill(highestAspectFirst, -1);
		highestAspectSecond = new int[n][2];
		
		aspectMining.setAspectWeights(aspectWeights);
	}
	
	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		aspectMining.sendFeedback(indexName, query,feedback);
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
	public String debug() {
		// TODO Auto-generated method stub
		return null;
	}


}
