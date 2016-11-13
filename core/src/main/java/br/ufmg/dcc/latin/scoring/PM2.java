package br.ufmg.dcc.latin.scoring;

import java.util.Arrays;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.RetrievalCache;

public class PM2 implements Scorer {

	float lambda;
	int[] highestAspect;
	
	float[] v;
	float[] s;
	float[][] coverage;

	float[] relevance;

	

	public PM2(){
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
		int q = highestAspect();
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


	@Override
	public void build(float[] params) {
		
		s = AspectCache.s;
		v = AspectCache.v;
		coverage = AspectCache.coverage;
		relevance = RetrievalCache.scores;
		lambda = params[1];
		int n = relevance.length;
		if (highestAspect == null){
			highestAspect = new int[n];
			Arrays.fill(highestAspect, -1);
		}

		
	}


	@Override
	public void flush() {
		coverage = AspectCache.coverage;
		s = AspectCache.s;
		v = AspectCache.v;
	}


}
