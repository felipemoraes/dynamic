package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.retrieval.ReScorerController;

public class MMR extends InteractiveReranker {

	private double lambda;

	protected String[] docsContent;
	private double[] cacheSim;
	
	private double[][] docSimCache;
	
	
	
	public MMR(){
	}

	
	public void update(int docid) {
		double[] newCache = null;
		if (docSimCache[docid] != null) {
			newCache = docSimCache[docid];
		} else {
			//float[] newCacheTitle = RetrievalController.getCosineSimilarities(docids, docid, indexName, "title");
			newCache = ReScorerController.sim(docids, docid);
			//newCacheTitle = normalize(newCacheTitle);
			newCache = normalize(newCache);
			//newCache = new float[newCacheContent.length];
			//float[] weights = RetrievalController.getFiedlWeights();
			//for (int i = 0; i < newCacheContent.length; i++) {
			//	newCache[i] =  newCacheContent[i];
			//}
			docSimCache[docid] = newCache;
		}

		
	    for (int i = 0; i < cacheSim.length; i++) {
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}
	    
	}
	
	@Override
	public void start(String query, String index){
		super.start(query,index);
		docSimCache = new double[relevance.length][];
	}

	@Override
	public void start(double[] params) {
		super.start(params);
		relevance = normalize(relevance);

		lambda = params[1];
		cacheSim = new double[relevance.length];
	}

	@Override
	public double score(int docid) {
		double score = lambda*(relevance[docid]) - (1-lambda)*cacheSim[docid];
		return score;
	}


	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
	}


	@Override
	public String debug() {
		return "";
	}

}
