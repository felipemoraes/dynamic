package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;

public class PM2 extends InteractiveReranker {

	float lambda;
	int[] highestAspect;
	
	private float[] v;
	private float[] s;
	private float[][] coverage;

	private String aspectMiningClassName;
	
	AspectMining aspectMining;

	public PM2(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	
	public int highestAspect(){
		int maxQ =  -1;
		float maxQuotient = -1;
		for (int i = 0; i < v.length; i++) {
			float quotient = v[i]/(2*s[i]+1);
			if (quotient > maxQuotient) {
				maxQ = i;
				maxQuotient = quotient;
			}
		}
		return maxQ;
	}
	
	public float score(int docid){
		
		int q = highestAspect();
		if (q == -1){
			return relevance[docid];
		}
		float quotientAspectq = v[q]/(2*s[q]+1);
		quotientAspectq *= coverage[docid][q];
		float quotientotherAspect  = 0;
		for (int i = 0; i < s.length; i++) {
			if (i != q) {
				
				quotientotherAspect += (v[i]/(2*s[i]+1))*coverage[docid][i];
			}
		}
		float score = lambda*quotientAspectq + (1-lambda)*quotientotherAspect;
		
		return score;
	}
	
	public void update(int docid){
		int q = highestAspect[docid];
		if (q == -1) {
			q = highestAspect();
		} 
		float allCoverage = 0;
		for (int i = 0; i < coverage[docid].length; ++i) {
			allCoverage += coverage[docid][i];
		}
		if (allCoverage > 0) {
			float newS = s[q] + coverage[docid][q]/allCoverage;
			s[q] = newS;
		} 
		highestAspect[docid] = q;
	}
	
	public void updateNovelty(){
		for (int j = 0; j < docids.length; ++j) {
			if (! selected.has(docids[j])) {
				continue;
			}
			update(j);
		}
	}
	
	@Override
	public void start(float[] params){
		super.start(params);
		relevance = normalize(relevance);
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName);
		coverage = aspectMining.getCoverage();
		v = aspectMining.getV();
		s = aspectMining.getS();
		lambda = params[1];
		int n = relevance.length;
		
		highestAspect = new int[n];
		Arrays.fill(highestAspect, -1);
		
	}


	@Override
	public void update(Feedback[] feedback) {
		aspectMining.miningFeedback(feedback);
		coverage = aspectMining.getCoverage();
		v = aspectMining.getV();
		s = aspectMining.getS();
		updateNovelty();
		
	}


	@Override
	public String debug(String topicid, int iteration) {
		return "";
	}

}
