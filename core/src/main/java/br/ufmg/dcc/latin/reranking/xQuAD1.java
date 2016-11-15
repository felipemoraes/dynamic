package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.controller.FlatAspectController;
import br.ufmg.dcc.latin.controller.RetrievalController;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xQuAD1 extends InteractiveReranker {

	
	float lambda;
	
	private FlatAspectController aspectControler;
	
	private float[] importance;
	private float[] novelty;
	private float[][] coverage;
	protected String[] docsContent;
	
	String indexName;
	
	@Override
	public void start(float[] params){
		super.start(params);
		relevance = normalize(relevance);
		aspectControler = new FlatAspectController();
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		docsContent = RetrievalCache.docsContent;
		indexName = RetrievalCache.indexName;
		lambda = params[1];
		novelty = new float[relevance.length];
		Arrays.fill(novelty, 1.0f);
	}
	
	@Override
	public float score(int docid) {
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i];
		}
		
		float score = (1-lambda)*relevance[docid] + lambda*diversity*novelty[docid];
		return score;
	}


	@Override
	public void update(int docid) {
		float[] probs = RetrievalController.getSimilarities(docids, docsContent[docid]);
	    probs = normalize(scaling(probs));
	    novelty[docid] *= (1-probs[docid]);
	}
	
	
	@Override
	public void update(Feedback[] feedback) {
		aspectControler.miningDiversityAspects(feedback);
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
	}



}
