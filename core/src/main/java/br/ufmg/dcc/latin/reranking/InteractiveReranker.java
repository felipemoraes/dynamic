package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.controller.AspectController;
import br.ufmg.dcc.latin.controller.FlatAspectController;
import br.ufmg.dcc.latin.controller.RetrievalController;
import br.ufmg.dcc.latin.diversity.scoring.Scorer;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;

public class InteractiveReranker implements Reranker {
	
	float[] relevance;
	int[] docids;
	String[] docnos;
	
	int depth;
	
	SelectedSet selected;
	AspectController aspectControler;
	
	private Scorer scorer;
	
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
			k++;
		}
		
		return result;
	}
	
	public void update(Feedback[] feedback){
		aspectControler.mining(feedback,scorer);
		scorer.flush();
	}
	
	public void start(String query, String index){
		ResultSet result = RetrievalController.search(query, index);
		docids = result.docids;
		relevance = result.scores;
		docnos = result.docnos;
		selected = new SelectedSet();
		aspectControler = new FlatAspectController();
	}
	
	public void start(float[] params){
		depth = (int) params[0];
		selected = new SelectedSet();
		aspectControler = new FlatAspectController();
	}

	public Scorer getScorer() {
		return scorer;
	}

	public void setScorer(Scorer scorer) {
		this.scorer = scorer;
	}
	
}
