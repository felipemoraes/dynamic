package br.ufmg.dcc.latin.reranking;

import java.util.HashMap;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.controller.RetrievalController;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;

public abstract class InteractiveReranker implements Reranker {
	
	protected float[] relevance;
	protected int[] docids;
	protected String[] docnos;
	
	protected int depth;
	
	protected SelectedSet selected;
	
	public abstract String debug(String topicid, int iteration);
	
	protected abstract float score(int docid);
	
	protected abstract void update(int docid);
	
	@Override
	public ResultSet get(){
		ResultSet result = new ResultSet(5);
		int depth = Math.min(relevance.length, this.depth+selected.size());
		
		// greedily diversify the top documents
		int k = 0;
		while(k < 5){
			
			float maxScore = Float.NEGATIVE_INFINITY;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (selected.has(docids[i])){
					continue;
				}
				float score = score(i);
				if (score > maxScore) {
					maxScore = score;
					maxRank = i;
				}
			}
			
			// update the score of the selected document
			
			result.scores[k] = maxScore;
			result.docids[k] = docids[maxRank];
			result.docnos[k] = docnos[maxRank];
			// mark as selected
			selected.put(docids[maxRank]);
			update(maxRank);
			k++;
		}
		
		return result;
	}
	
	public abstract void update(Feedback[] feedback);
	
	public void start(String query, String index){
		RetrievalController.setFiedlWeights(new float[]{0.2f,0.8f});
		ResultSet result = RetrievalController.search(query, index);
		docids = result.docids;
		relevance = result.scores;
		docnos = result.docnos;
		selected = new SelectedSet();
		RetrievalCache.passageCache = new HashMap<String,float[]>();
	}
	
	public void start(float[] params){
		depth = (int) params[0];
		selected = new SelectedSet();
	}


}
