package br.ufmg.dcc.latin.reranking;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import br.ufmg.dcc.latin.cache.SearchCache;
import br.ufmg.dcc.latin.querying.CollectionResultSet;
import br.ufmg.dcc.latin.querying.QueryResultSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;
import br.ufmg.dcc.latin.searching.SearchEngineManager;

public class MMRReranker extends StaticReranker{

	
	private float[] cacheSim;

	private int n;
	private int depth;
	private float lambda;
	
	public MMRReranker(int depth, float lambda){
		this.depth = depth;
		this.lambda = lambda;
		selected = new SelectedSet();
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
			updateCache(SearchCache.indexName, docids,docsContent[maxRank]);
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
	
	private void updateCache(String content, int[] docids, String indexName) {
		
		QueryParser queryParser = SearchEngineManager.getQueryParser();
		BooleanQuery.setMaxClauseCount(200000);
		IndexSearcher searcher = SearchEngineManager.getIndexSearcher(indexName);
		Query q = null;
		 float[] newCache = new float[n];
		 Arrays.fill(newCache, 0);
		try {
			q = queryParser.parse(QueryParser.escape(content));
			for (int i = 0; i < docids.length; i++) {
				Explanation exp = searcher.explain(q, docids[i]);
				newCache[i] = exp.getValue();
			}
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
		
	    
	    newCache = scaling(newCache);
	    
	    for (int i = 0; i < newCache.length; i++) {
			if (cacheSim[i] < newCache[i]) {
				cacheSim[i] = newCache[i];
			}
		}

	    
	}

}
