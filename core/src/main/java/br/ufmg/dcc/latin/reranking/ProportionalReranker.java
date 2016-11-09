package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.querying.QueryResultSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;
import br.ufmg.dcc.latin.scoring.diversity.PM2;

public class ProportionalReranker extends InteractiveReranker {
	
	int depth;
	float lambda;
	PM2 scorer;
	int[] highestAspect;
	int n;
	
	public ProportionalReranker(PM2 scorer, int depth, float lambda){
		this.scorer = scorer;
		this.depth = depth;
		this.lambda = lambda;
	}
	
	@Override
	public ResultSet reranking(ResultSet baselineResultSet) {
		
		
		float[] relevance = normalize(baselineResultSet.getScores());
		int[] docids = baselineResultSet.getDocids();
		
		n = docids.length;
	
		if (highestAspect == null){
			highestAspect = new int[n];
			Arrays.fill(highestAspect, -1);
		}
		
		float[] scores = new float[n];
		Arrays.fill(scores, 0f);
		SelectedSet localSelected = new SelectedSet();
		depth = Math.min(relevance.length, depth+getSelected().size());
		// greedily diversify the top documents
		while(localSelected.size() < depth-getSelected().size()){
			
			// select query aspect with highest quotient
			int q = scorer.highestAspect();
			
			float maxScore = -1;
			int maxRank = -1;
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (localSelected.has(docids[i]) || getSelected().has(docids[i])){
					continue;
				}
				float score = relevance[i];
				
				if (q != -1) {
					score = scorer.score(i, q);
				}
				if (score > maxScore) {
					maxScore = score;
					maxRank = i;
				}
			}
			
			// update the score of the selected document
			scores[maxRank] = maxScore;
			// mark as selected
			localSelected.put(docids[maxRank]);
			if (q != -1) {
				scorer.update(maxRank,q);
				highestAspect[maxRank] = q;
			}
			
		}
		for (int i = depth; i < n; i++) {
			scores[i] = (1-lambda) * relevance[i];
		}
		
		QueryResultSet finalResultSet = new QueryResultSet();
		
		finalResultSet.setDocids(docids);
		finalResultSet.setScores(scores);
		finalResultSet.setDocnos(baselineResultSet.getDocnos());
		
		return finalResultSet;
	}
	
	public void updateProportional(){
		if (highestAspect == null){
			return;
		}
		for (int i = 0; i < highestAspect.length; i++) {
			if (highestAspect[i] != -1){
				scorer.update(i, highestAspect[i]);
			}
		}
	}



}
