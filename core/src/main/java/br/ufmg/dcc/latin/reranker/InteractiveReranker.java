package br.ufmg.dcc.latin.reranker;

import org.apache.lucene.search.similarities.DPH;

import br.ufmg.dcc.latin.baseline.BaselineRanker;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.BooleanSelectedSet;
import br.ufmg.dcc.latin.querying.ResultSet;

public abstract class InteractiveReranker implements Reranker {
	
	protected double[] relevance;
	protected int[] docids;
	protected String[] docnos;
	
	protected int depth;
	
	protected String query;
	protected String indexName;
	
	protected BooleanSelectedSet selected;
	
	public abstract String debug();
	
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
			// mark as selected
			localSelectedSet.put(maxRank);
			update(maxRank);
			k++;
		}
		
		return result;
	}
	
	public void update(Feedback[] feedback){
		for (int i = 0; i < feedback.length; i++) {
			selected.put(feedback[i].index);
		}
	}
	
	public void start(String query, String index){
		this.query = query;
		this.indexName = index;
		BaselineRanker baselineRanker = BaselineRanker.getInstance(new DPH(), new double[]{0.15f,0.85f});
		// TODO
		ResultSet result = baselineRanker.search("", query, index);
		docids = result.docids;
		relevance = result.scores;
		docnos = result.docnos;
		selected = new BooleanSelectedSet(docnos.length);
	}
	
	public void start(double[] params){
		depth = (int) params[0];
		selected = new BooleanSelectedSet(docnos.length);
	}

	public void setParams(double[] params){
		depth = (int) params[0];
	}

}
