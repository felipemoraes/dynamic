package br.ufmg.dcc.latin.reranker;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.BooleanSelectedSet;
import br.ufmg.dcc.latin.querying.ResultSet;

public abstract class InteractiveReranker implements Reranker {
	
	public double[] relevance;
	public int[] docids;
	public String[] docnos;
	
	protected String query;
	protected String indexName;
	
	public BooleanSelectedSet selected;
	
	protected FeedbackModeling feedbackModeling;

	protected abstract double score(int docid);
	
	protected abstract void update(int docid);
	
	public int depth;
	
	
	public void update(Feedback[] feedback){
		
	}
	
	public InteractiveReranker(FeedbackModeling feedbackModeling){
		this.feedbackModeling = feedbackModeling;
	}
	
	@Override
	public ResultSet get(){
		ResultSet result = new ResultSet(5);
		
		int depth = Math.min(relevance.length, this.depth+selected.size());	
		// greedily diversify the top documents
		int k = 0;
		while(k < 5){
			double maxScore = Double.NEGATIVE_INFINITY;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (selected.has(i)){
					continue;
				}
				double score = score(i);
				if (score > maxScore) {
					maxScore = score;
					maxRank = i;
				}
			}
			
			// update the score of the selected document
			if (maxRank < 0) {
				break;
			}
			
			result.scores[k] = maxScore;
			result.docids[k] = docids[maxRank];
			result.docnos[k] = docnos[maxRank];
			result.index[k] = maxRank;
			// mark as selected
			selected.put(maxRank);
			update(maxRank);
			k++;
		}
		
		for (int i = k; i < 5; i++) {
			result.docnos[k] = null;
		}
		
		return result;
	}

	
	public void start(ResultSet resultSet, double[] params){
		depth = 1000;
		docids = resultSet.docids;
		relevance = resultSet.scores;
		docnos = resultSet.docnos;
		selected = new BooleanSelectedSet(docnos.length);
	}

}
