package br.ufmg.dcc.latin.reranking;

import java.io.IOException;
import java.util.Arrays;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;

import br.ufmg.dcc.latin.querying.CollectionResultSet;
import br.ufmg.dcc.latin.querying.QueryResultSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;

public class MMRReranker extends StaticReranker{

	
	private float[] cacheSim;
	
	private static Analyzer analyzer = new StandardAnalyzer();
	private static Similarity similarity = new ClassicSimilarity();
	private Directory indexDir;
	private int n;
	private int depth;
	private float lambda;
	
	public MMRReranker(Directory indexDir, int depth, float lambda){
		this.indexDir = indexDir;
		this.depth = depth;
		this.lambda = lambda;
		selected = new SelectedSet();
	}
	
	@Override
	public ResultSet reranking(ResultSet baselineResultSet) {
		CollectionResultSet collectionResultSet = (CollectionResultSet) baselineResultSet;
		
		float[] relevance = normalize(baselineResultSet.getScores());
		int[] docids = baselineResultSet.getDocids();
		String[] docsContent = collectionResultSet.getDocsContent();
		n = docids.length;
		
		float[] scores = new float[n];
		Arrays.fill(scores, 0f);
		depth = Math.min(relevance.length, depth);
		
		cacheSim = new float[depth];
		Arrays.fill(cacheSim, 0.0f);
		SelectedSet selected = new SelectedSet();
		while (selected.size() < depth) {
			float maxScore = Float.NEGATIVE_INFINITY;
			int maxRank = -1;
			// greedily select max document
			for (int i = 0; i < depth; ++i){
				if (selected.has(i)){
					continue;
				}
				
				float score = lambda*(relevance[i]) - (1-lambda)*cacheSim[i];
				if (score > maxScore){
					maxRank = i;
					maxScore = score;
				}
			}
			selected.put(maxRank);
			updateCache(maxRank,docsContent[maxRank]);
			scores[maxRank] = maxScore;
			
		}
		
		for (int i = depth; i < n; i++) {
			scores[i] = lambda * relevance[i] - (1-lambda) *cacheSim[i];
		}
		QueryResultSet finalResultSet = new QueryResultSet();
		
		finalResultSet.setDocids(docids);
		finalResultSet.setScores(scores);
		finalResultSet.setDocnos(baselineResultSet.getDocnos());
		
		return finalResultSet;
	}
	
	private void updateCache(int selected, String content) {
		
		QueryParser queryParser = new QueryParser("content", analyzer);
		BooleanQuery.setMaxClauseCount(200000);
		
		Query q = null;
		try {
			q = queryParser.parse(QueryParser.escape(content).toLowerCase());
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	    IndexReader reader = null;
		try {
			reader = DirectoryReader.open(indexDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    IndexSearcher searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(similarity);
	    int hitsPerPage = n;
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
	    
	    try {
			searcher.search(q, collector);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	   
	    
	    
	    for(int i = 0;i<hits.length;++i) {
	    	int cache_ix = hits[i].doc;
	    	if (cacheSim[cache_ix] < hits[i].score){
	    		cacheSim[cache_ix] = hits[i].score;
	    	}
	    }

	    try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}

}
