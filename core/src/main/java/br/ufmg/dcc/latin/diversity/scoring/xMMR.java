package br.ufmg.dcc.latin.diversity.scoring;

import java.util.Arrays;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.RetrievalCache;

public class xMMR implements Scorer {

	private float[] cacheSim;
	
	private int n;

	private float lambda;
	
	private float[] relevance;
	
	private float[][] coverage; 

	
	public xMMR(){

	}



	@Override
	public void build(float[] params) {
		relevance = RetrievalCache.scores;
		
		coverage = AspectCache.coverage;
		lambda = params[1];
		
	}

	@Override
	public float score(int docid) {
		float score = lambda*(relevance[docid]) - (1-lambda)*cacheSim[docid];
		return score;
	}

	@Override
	public void flush() {
		coverage = AspectCache.coverage;
	}

	@Override
	public void update(int docid) {
		float[] newCache = new float[n];
	    Arrays.fill(newCache, 0);
		if (coverage == null) {
			return;
		}
	    for(int i = 0;i<newCache.length;++i) {
	    	newCache[i] = cosine(coverage[i],coverage[docid]);
	    }
	    
	   // newCache = scaling(newCache);
	    
	    for (int i = 0; i < newCache.length; i++) {
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}
	    
		
	}
	
	private float cosine(float[] v1, float[] v2){
		float denom = 0;
		float sum1 = 0;
		float sum2  = 0;
	
		for (int i = 0; i < v2.length; i++) {
			denom += v1[i]*v2[i];
		}
		
		for (int i = 0; i < v2.length; i++) {
			sum1 += v1[i]*v1[i];
			sum2 += v2[i]*v2[i];
		}
		sum1 = (float) Math.sqrt(sum1);
		sum2 = (float) Math.sqrt(sum2);
		
		if (sum1*sum2 > 0){
			return denom/(sum1*sum2);
		} 
		
		return 0;
	}
	

	
}
