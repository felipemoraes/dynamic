package br.ufmg.dcc.latin.dynamicreranker;

import br.ufmg.dcc.latin.aspectmodeling.AspectModel;
import br.ufmg.dcc.latin.baselineranker.ResultList;
import br.ufmg.dcc.latin.utils.SharedCache;

public abstract class DynamicReranker {
	
	protected BooleanSelectedSet selected;
	
	protected int[] docids;
	protected String[] docnos;
	protected double[] relevance;
	
	protected int depth;
	
	protected abstract double score(int docid);
	protected abstract void update(int docid);
	
	public ResultList getResultList(AspectModel aspectModel){
		
		ResultList result = new ResultList(5);
		double[] relevance = SharedCache.scores;
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
			result.scores[k] = maxScore;
			result.docids[k] = docids[maxRank];
			result.docnos[k] = docnos[maxRank];
			
			// mark as selected
			selected.put(maxRank);
			update(maxRank);
			k++;
		}
		
		return result;
	}
	
	
	
	protected class BooleanSelectedSet {
		boolean[] selected;
		
		int size;
		public BooleanSelectedSet(int n) {
			selected = new boolean[n];
		}
		public boolean has(int d){
			return selected[d];
		}
		
		public void put(int d){
			if (selected[d] == false) {
				selected[d] = true;
				size++;
			}
		}
		
		public int size(){
			return size;
		}
	}
}
