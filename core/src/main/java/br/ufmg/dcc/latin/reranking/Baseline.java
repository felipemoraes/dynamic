package br.ufmg.dcc.latin.reranking;

import org.apache.lucene.search.similarities.BM25;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.search.similarities.LMDirichlet;

import br.ufmg.dcc.latin.controller.RetrievalController;
import br.ufmg.dcc.latin.feedback.Feedback;

public class Baseline extends InteractiveReranker {
	
	public Baseline(String sim){
		if (sim.equals("LM")){
			RetrievalController.setSimilarity(new LMDirichlet(2500.0f));
		} else if (sim.equals("DPH")){
			RetrievalController.setSimilarity(new DPH());
		} else if (sim.equals("BM25")){
			RetrievalController.setSimilarity(new BM25());
		} 
	}

	@Override
	protected float score(int docid) {
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
