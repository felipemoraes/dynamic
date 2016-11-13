package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.diversity.Aspect;
import br.ufmg.dcc.latin.querying.QueryResultSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;

public class xMMRReranker extends StaticReranker {

	private float[] cacheSim;
	
	private int n;
	private int depth;
	private float lambda;
	
	private Aspect[][] coverage; 
	
	public xMMRReranker(int depth, float lambda){
		this.depth = depth;
		this.lambda = lambda;
		selected = new SelectedSet();
	}
	
	@Override
	public ResultSet getTopResults(ResultSet baselineResultSet) {
		
		
		int[] resultDocids = new int[5];
		String[] resultDocnos = new String[5];
		float[] resultScores = new float[5];
		
		float[] relevance = scaling(baselineResultSet.getScores());
		int[] docids = baselineResultSet.getDocids();
		String[] docnos = baselineResultSet.getDocnos();
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
			updateCache(maxRank);
			
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

	private float cosine(Aspect[] v1, Aspect[] v2){
		float denom = 0;
		float sum1 = 0;
		float sum2  = 0;
	
		for (int i = 0; i < v2.length; i++) {
			denom += v1[i].getValue()*v2[i].getValue();
		}
		
		for (int i = 0; i < v2.length; i++) {
			sum1 += v1[i].getValue()*v1[i].getValue();
			sum2 += v2[i].getValue()*v2[i].getValue();
		}
		sum1 = (float) Math.sqrt(sum1);
		sum2 = (float) Math.sqrt(sum2);
		
		if (sum1*sum2 > 0){
			return denom/(sum1*sum2);
		} 
		
		return 0;
	}
	
	private void updateCache(int d) {
		
		float[] newCache = new float[n];
	    Arrays.fill(newCache, 0);
		if (coverage == null) {
			return;
		}
	    for(int i = 0;i<newCache.length;++i) {
	    	newCache[i] = cosine(coverage[i],coverage[d]);
	    }
	    
	    newCache = scaling(newCache);
	    
	    for (int i = 0; i < newCache.length; i++) {
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}
	    
	}

	public Aspect[][] getCoverage() {
		return coverage;
	}

	public void setCoverage(Aspect[][] coverage) {
		this.coverage = coverage;
	}
	
	
}
