package br.ufmg.dcc.latin.reranking;

import org.apache.lucene.search.similarities.TFIDF;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.controller.RetrievalController;
import br.ufmg.dcc.latin.feedback.Feedback;

public class MMR extends InteractiveReranker {

	private float lambda;

	protected String[] docsContent;
	private float[] cacheSim;
	
	private float[][] docSimCache;
	
	String indexName;
	
	public MMR(){
		
	}

	
	public void update(int docid) {
		float[] newCache = null;
		if (docSimCache[docid] != null) {
			newCache = docSimCache[docid];
		} else {
			newCache = RetrievalController.getSimilarities(docids, docid);
			newCache = normalize(newCache);
			docSimCache[docid] = newCache;
		}

	    
	    for (int i = 0; i < newCache.length; i++) {
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}

	}
	@Override
	public void start(String query, String index){
		super.start(query,index);
		RetrievalController.setSimilarity(new TFIDF());
		RetrievalController.termsVector = null;
		docSimCache = new float[relevance.length][];
	}

	@Override
	public void start(float[] params) {
		super.start(params);
		relevance = normalize(relevance);
		docsContent = RetrievalCache.docsContent;
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


	@Override
	public String debug(String topicid, int iteration) {
		return "";
	}

}
