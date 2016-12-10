package br.ufmg.dcc.latin.reranking;

import java.util.HashMap;

import org.apache.lucene.search.similarities.BM25;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.search.similarities.LMDirichlet;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class Baseline extends InteractiveReranker {
	
	private String query;
	private String index;
	
	public Baseline(String sim){
		if (sim.equals("LM")){
			RetrievalController.setSimilarity(new LMDirichlet(2000.0f));
		} else if (sim.equals("DPH")){
			RetrievalController.setSimilarity(new DPH());
		} else if (sim.equals("BM25")){
			RetrievalController.setSimilarity(new BM25());
		} 
	}
	
	@Override
	public void start(double[] params) {
		
		super.start(params);
		double[] fiedlWeights = new double[2];
		fiedlWeights[0] = params[1];
		fiedlWeights[1] = 1-params[1];
		RetrievalController.setFiedlWeights(fiedlWeights);
		RetrievalController.parser = null;
		ResultSet result = RetrievalController.search(query, index);
		docids = result.docids;
		relevance = result.scores;
		docnos = result.docnos;
		selected = new SelectedSet();
		RetrievalCache.passageCache = new HashMap<String,double[]>();
	}
	
	@Override
	public void start(String query, String index){
		this.query = query;
		this.index = index;
	}

	@Override
	protected double score(int docid) {
		return relevance[docid];
	}

	@Override
	protected void update(int docid) {
		
	}

	@Override
	public void update(Feedback[] feedback) {
	}

	@Override
	public String debug(String topicid, int iteration) {
		return "";
	}

}
