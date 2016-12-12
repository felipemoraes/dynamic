package br.ufmg.dcc.latin.reranking;

import java.util.HashMap;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.BooleanSelectedSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public abstract class InteractiveReranker implements Reranker {
	
	protected double[] relevance;
	protected int[] docids;
	protected String[] docnos;
	
	protected int depth;
	
	protected String query;
	protected String indexName;
	
	protected BooleanSelectedSet selected;
	
	public abstract String debug(String topicid, int iteration);
	
	protected abstract double score(int docid);
	
	protected abstract void update(int docid);
	
	@Override
	public ResultSet get(){
		ResultSet result = new ResultSet(5);
		int depth = Math.min(relevance.length, this.depth+selected.size());	
		BooleanSelectedSet localSelectedSet = new BooleanSelectedSet(docnos.length);
		// greedily diversify the top documents
		int k = 0;
		while(k < 5){
			
			double maxScore = Double.NEGATIVE_INFINITY;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (selected.has(i) || localSelectedSet.has(i)){
					continue;
				}
				double score = score(i);
				if (score > maxScore) {
					maxScore = score;
					maxRank = i;
				}
			}
			
			// update the score of the selected document
			
			result.scores[k] = maxScore;
			result.docids[k] = docids[maxRank];
			result.docnos[k] = docnos[maxRank];
			result.indices[k] = maxRank;
			// mark as selected
			localSelectedSet.put(maxRank);
			update(maxRank);
			k++;
		}
		
		return result;
	}
	
	public void update(Feedback[] feedback){
		for (int i = 0; i < feedback.length; i++) {
			selected.put(feedback[i].getIndex());
		}
	}
	
	public void start(String query, String index){
		this.query = query;
		this.indexName = index;
		RetrievalController.setFiedlWeights(new double[]{0.15f,0.85f});
		ResultSet result = RetrievalController.search(RetrievalCache.topicId, query, index);
		docids = result.docids;
		relevance = result.scores;
		docnos = result.docnos;
		selected = new BooleanSelectedSet(docnos.length);
		RetrievalCache.passageCache = new HashMap<String,double[]>();
		RetrievalCache.subtopicsCache = new HashMap<String,double[]>();
	}
	
	public void start(double[] params){
		depth = (int) params[0];
		selected = new BooleanSelectedSet(docnos.length);
	}

	public void setParams(double[] params){
		depth = (int) params[0];
	}

}
