package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.controller.RetrievalController;
import br.ufmg.dcc.latin.feedback.Feedback;

public class MMR extends InteractiveReranker {

	private float lambda;
	float[] relevance;
	int[] docids;
	protected String[] docsContent;
	private float[] cacheSim;
	String indexName;
	
	public MMR(){
		
	}

	
	public void update(int docid) {
		
		float[] newCache = RetrievalController.getSimilarities(docids, docsContent[docid]);
	    
	    newCache = scaling(newCache);
	    
	    for (int i = 0; i < newCache.length; i++) {
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}

	}

	@Override
	public void start(float[] params) {
		super.start(params);
		relevance = scaling(RetrievalCache.scores);
		docsContent = RetrievalCache.docsContent;
		docids = RetrievalCache.docids;
		indexName = RetrievalCache.indexName;
		
		lambda = params[1];
		cacheSim = new float[relevance.length];
	}

	@Override
	public float score(int docid) {
		float score = lambda*(relevance[docid]) - (1-lambda)*cacheSim[docid];
		return score;
	}


	@Override
	public void update(Feedback[] feedback) {
	}

}
