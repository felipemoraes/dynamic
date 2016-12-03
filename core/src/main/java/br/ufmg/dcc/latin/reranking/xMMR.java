package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xMMR extends InteractiveReranker {

	private float[] cacheSim;
	
	private int n;

	private float lambda;
	
	private float[] relevance;
	
	private float[][] coverage; 

	private AspectMining aspectMining;
	private String aspectMiningClassName;
	
	public xMMR(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}



	@Override
	public void start(float[] params) {
		super.start(params);
		relevance = normalize(RetrievalCache.scores);
		n = relevance.length;
		cacheSim = new float[n];
		lambda = params[1];
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		coverage = aspectMining.getCoverage();
	}

	@Override
	public float score(int docid) {
		
		float score = lambda*(relevance[docid]) - (1-lambda)*cacheSim[docid];
		return score;
	}


	@Override
	public void update(int docid) {
		
		float[] newCache = new float[n];
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

	@Override
	public void update(Feedback[] feedback) {
		aspectMining.miningFeedback(indexName, query, feedback);
		coverage = aspectMining.getCoverage();
		
	}



	@Override
	public String debug(String topicid, int iteration) {
		return "";
	}
	

	
}
