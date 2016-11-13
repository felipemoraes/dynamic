package br.ufmg.dcc.latin.scoring;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.querying.SelectedSet;

public class xQuAD implements Scorer {

	float lambda;
	
	SelectedSet selected;
	
	float[] importance;
	float[] novelty;
	float[][] coverage;
	float[] relevance;
	
	public xQuAD(){
		
	}
	
	@Override
	public void build(float[] params){
		importance = AspectCache.importance;
		novelty = AspectCache.novelty;
		coverage = AspectCache.coverage;
		relevance = RetrievalCache.scores;
		lambda = params[1];
	}

	@Override
	public float score(int docid){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i]*novelty[i];
		}
		float score = lambda*relevance[docid] + (1-lambda)*diversity;
		
		return score;
	}
	
	@Override
	public void flush(){
		importance = AspectCache.importance;
		novelty = AspectCache.novelty;
		coverage = AspectCache.coverage;
	}
	
	@Override
	public void update(int docid){
		for (int i = 0; i < novelty.length; i++) {
			novelty[i] *= (1-coverage[docid][i]);
		}
	}

}
