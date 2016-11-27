package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class xQuAD1 extends InteractiveReranker {

	
	float lambda;
	
	private String aspectMiningClassName;
	
	
	private float[] importance;
	private float[] novelty;
	private float[][] coverage;
	private float[][] docSimCache;
	protected String[] docsContent;
	
	String indexName;
	
	private float fieldWeight;
	
	private AspectMining aspectMining;
	
	public xQuAD1(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	@Override
	public void start(String query, String index){
		super.start(query,index);
		indexName = index;
		RetrievalController.termsVector = null;
		docSimCache = new float[relevance.length][];
	}
	
	
	
	@Override
	public void start(float[] params){
		super.start(params);
		relevance = normalize(relevance);
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
	
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
		float[] probs = null;
		
		if (docSimCache[docid] != null) {
			probs = docSimCache[docid];
		} else {
			float[] newCacheTitle = RetrievalController.getCosineSimilarities(docids, docid,indexName,"title");
			float[] newCacheContent = RetrievalController.getCosineSimilarities(docids, docid,indexName,"content");
			newCacheTitle = normalize(newCacheTitle);
			newCacheContent = normalize(newCacheContent);
			probs = new float[newCacheContent.length];
			for (int i = 0; i < newCacheContent.length; i++) {
				probs[i] = (1-fieldWeight) *newCacheTitle[i] + fieldWeight*newCacheContent[i];
			}
			docSimCache[docid] = probs;
		}	
		
		for (int i = 0; i < probs.length; i++) {
			novelty[i] *= (1-probs[i]);
		}
		
	    
	}
	
	
	@Override
	public void update(Feedback[] feedback) {
		aspectMining.miningFeedback(indexName, query,feedback);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
	}

	@Override
	public String debug(String topicid, int iteration) {
		return "";
	}



}
