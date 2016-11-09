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

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.querying.CollectionResultSet;
import br.ufmg.dcc.latin.querying.QueryResultSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;
import br.ufmg.dcc.latin.scoring.DiversityScorer;


public class DiversityReranker extends InteractiveReranker {
	
	DiversityScorer scorer;
	private static Analyzer analyzer = new StandardAnalyzer();
	private static Similarity similarity = new ClassicSimilarity();
	private Directory indexDir;
	int depth;
	float lambda;
	
	public DiversityReranker(DiversityScorer scorer, int depth, float lambda){
		this.scorer = scorer;
		this.depth = depth;
		this.lambda = lambda;
	}
	
	public DiversityReranker(DiversityScorer scorer, Directory indexDir, int depth, float lambda){
		this.scorer = scorer;
		this.depth = depth;
		this.lambda = lambda;
		this.indexDir = indexDir;
	}
	
	public float[] similarities(int selected, String content){
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
	    int hitsPerPage = AspectCache.n;
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
	    
	    try {
			searcher.search(q, collector);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	    float[] newCache = new float[AspectCache.n];
	    Arrays.fill(newCache, 0);
	    
	    for(int i = 0;i<hits.length;++i) {
	    	int cache_ix = hits[i].doc;
	    	newCache[cache_ix] = hits[i].score;
	    }
	    
	    newCache = scaling(newCache);
	    
	    try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return newCache;
	}
	
	@Override
	public ResultSet reranking(ResultSet baselineResultSet) {
		
		CollectionResultSet collectionResultSet = (CollectionResultSet) baselineResultSet;
		String[] docsContent = collectionResultSet.getDocsContent();
		float[] relevance = normalize(baselineResultSet.getScores());
		int[] docids = baselineResultSet.getDocids();
		
		int n = docids.length;
		
		float[] scores = new float[n];
		Arrays.fill(scores, 0f);
		
		depth = Math.min(relevance.length, depth+getSelected().size());
		
		SelectedSet localSelected = new SelectedSet();
		
		// greedily diversify the top documents
		while(localSelected.size() < depth-getSelected().size()){
			
			float maxScore = -1;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (localSelected.has(docids[i]) || getSelected().has(docids[i])){
					continue;
				}
				
				float score = (1-lambda)*relevance[i] + lambda*scorer.div(i);
				
				if (score > maxScore) {
					maxScore = score;
					maxRank = i;
				}
			}
			
			// update the score of the selected document
			scores[maxRank] = maxScore;
			
			// mark as selected
			localSelected.put(docids[maxRank]);
			float[] sims = similarities(maxRank, docsContent[maxRank]);
			scorer.update(sims);
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
	
	@Override
	public ResultSet reranking(ResultSet baselineResultSet, boolean sim) {
		
		float[] relevance = normalize(baselineResultSet.getScores());
		int[] docids = baselineResultSet.getDocids();
		
		int n = docids.length;
		
		float[] scores = new float[n];
		Arrays.fill(scores, 0f);
		
		depth = Math.min(relevance.length, depth+getSelected().size());
		
		SelectedSet localSelected = new SelectedSet();
		
		// greedily diversify the top documents
		while(localSelected.size() < depth-getSelected().size()){
			
			float maxScore = -1;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (localSelected.has(docids[i]) || getSelected().has(docids[i])){
					continue;
				}
				
				float score = (1-lambda)*relevance[i] + lambda*scorer.div(i);
				
				if (score > maxScore) {
					maxScore = score;
					maxRank = i;
				}
			}
			
			// update the score of the selected document
			scores[maxRank] = maxScore;
			
			// mark as selected
			localSelected.put(docids[maxRank]);
			scorer.update(maxRank);
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

}
