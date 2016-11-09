package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.querying.CollectionResultSet;
import br.ufmg.dcc.latin.querying.QueryResultSet;
import br.ufmg.dcc.latin.querying.ResultSet;

public class xMMRReranker extends StaticReranker {

	private float[] cacheSim;
	
	private int n;
	private int depth;
	private float lambda;
	
	
	public xMMRReranker(){
		
	}
	
	@Override
	public ResultSet getTopResults(ResultSet baselineResultSet) {
		CollectionResultSet collectionResultSet = (CollectionResultSet) baselineResultSet;
		
		int[] resultDocids = new int[5];
		String[] resultDocnos = new String[5];
		float[] resultScores = new float[5];
		
		float[] relevance = scaling(baselineResultSet.getScores());
		int[] docids = baselineResultSet.getDocids();
		String[] docnos = baselineResultSet.getDocnos();
		String[] docsContent = collectionResultSet.getDocsContent();
		n = docids.length;
		
		float[] scores = new float[n];
		Arrays.fill(scores, 0f);
		depth = Math.min(relevance.length, depth);
		
		cacheSim = new float[depth];
		Arrays.fill(cacheSim, 0.0f);
		int k = 0;
		
		while (k < 5) {
			float maxScore = Float.NEGATIVE_INFINITY;
			int maxRank = -1;
			// greedily select max document
			for (int i = 0; i < depth; ++i){
				if (selected.has(docids[i])){
					continue;
				}
				
				float score = lambda*(relevance[i]) - (1-lambda)*cacheSim[i];
				
				if (score > maxScore){
					maxRank = i;
					maxScore = score;
				}
			}
			
			selected.put(docids[maxRank]);
			updateCache(maxRank,docsContent[maxRank]);
			resultScores[k] = maxScore;
			resultDocids[k] = docids[maxRank];
			resultDocnos[k] = docnos[maxRank];
			
			k++;
		}
		

		QueryResultSet finalResultSet = new QueryResultSet();
		
		finalResultSet.setDocids(resultDocids);
		finalResultSet.setScores(resultScores);
		finalResultSet.setDocnos(resultDocnos);
		
		return finalResultSet;
	}

	private void updateCache(int maxRank, String string) {
		// TODO Auto-generated method stub
		
	}
	
	
}
