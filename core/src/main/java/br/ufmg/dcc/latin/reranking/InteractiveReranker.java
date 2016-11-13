package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.controller.AspectController;
import br.ufmg.dcc.latin.controller.RetrievalController;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;
import br.ufmg.dcc.latin.scoring.Scorer;

public class InteractiveReranker implements Reranker {
	
	float[] relevance;
	int[] docids;
	String[] docnos;
	
	int depth;
	
	SelectedSet selected;
	AspectController aspectControler;
	
	Scorer scorer;
	
	@Override
	public ResultSet get(){
		ResultSet result = new ResultSet(5);
		int depth = Math.min(relevance.length, this.depth+selected.size());
		
		// greedily diversify the top documents
		int k = 0;
		while(k < 5){
			
			float maxScore = -1;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (selected.has(docids[i])){
					continue;
				}
				float score = scorer.score(i);
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
			scorer.update(maxRank);
		}
		
		return result;
	}
	
	public void update(Feedback[] feedback){
		aspectControler.mining(feedback,scorer);
	}
	
	public void start(String query, String index, float[] params){
		depth = (int) params[0];
		ResultSet result = RetrievalController.search(query, index);
		docids = result.docids;
		relevance = result.scores;
		docnos = result.docnos;
	}
	
	public void start(float[] params){
		depth = (int) params[0];
	}
	
}
