package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import org.apache.lucene.search.similarities.LMDirichlet;
import org.apache.lucene.search.similarities.TFIDF;

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
	private float[][] docSimCache;
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
		docSimCache =  new float[relevance.length][];
		RetrievalController.setSimilarity(new LMDirichlet(2500f));
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
		float[] probs = null;
		
		if (docSimCache[docid] != null) {
			probs = docSimCache[docid];
		} else {
			RetrievalController.setSimilarity(new TFIDF());
			probs = RetrievalController.getSimilarities(docids, docid);
			probs = normalize(probs);
			docSimCache[docid] = probs;
		}

		
	    novelty[docid] *= (1-probs[docid]);
	}
	
	
	@Override
	public void update(Feedback[] feedback) {
		aspectControler.miningDiversityAspects(feedback);
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
	}

	@Override
	public String debug(String topicid, int iteration) {
		return "";
	}



}
