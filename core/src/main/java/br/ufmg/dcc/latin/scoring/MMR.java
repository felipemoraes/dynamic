package br.ufmg.dcc.latin.scoring;

import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.controller.RetrievalController;

public class MMR implements Scorer{

	private float lambda;
	float[] relevance;
	int[] docids;

	String[] docsContent;
	private float[] cacheSim;
	String indexName;
	private Similarity f2;
	
	public MMR(){
		
	}
	

	
	
	public void update(int docid) {
		
		float[] newCache = RetrievalController.getSimilarities(docids, docsContent[docid], f2);
	    
	    //newCache = scaling(newCache);
	    
	    for (int i = 0; i < newCache.length; i++) {
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}

	}





	@Override
	public void build(float[] params) {
		
		relevance = RetrievalCache.scores;
		docsContent = RetrievalCache.docsContent;
		docids = RetrievalCache.docids;
		indexName = RetrievalCache.indexName;
		f2 = new ClassicSimilarity();
		lambda = params[1];
		
	}

	@Override
	public float score(int docid) {
		float score = lambda*(relevance[docid]) - (1-lambda)*cacheSim[docid];
		return score;
	}

	@Override
	public void flush() {
	}


}
