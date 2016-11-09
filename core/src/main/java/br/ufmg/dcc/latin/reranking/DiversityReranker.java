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
import br.ufmg.dcc.latin.diversity.Aspect;
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
		BooleanQuery.setMaxClauseCount(400000);
		
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
	

	public ResultSet rerankingandTopResults(ResultSet baselineResultSet, boolean sim) {
		
		CollectionResultSet collectionResultSet = (CollectionResultSet) baselineResultSet;
		String[] docsContent = collectionResultSet.getDocsContent();
		float[] relevance = normalize(baselineResultSet.getScores());
		int[] docids = baselineResultSet.getDocids();
		String[] docnos = baselineResultSet.getDocnos();
		int[] resultDocids = new int[5];
		String[] resultDocnos = new String[5];
		float[] resultScores = new float[5];
		
		int n = docids.length;
		
		float[] scores = new float[n];
		Arrays.fill(scores, 0f);
		
		depth = Math.min(relevance.length, depth+getSelected().size());
		
		SelectedSet localSelected = new SelectedSet();
		
		// greedily diversify the top documents
		int k = 0;
		while(k < 5){
			
			float maxScore = -1;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (selected.has(docids[i])){
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
			resultScores[k] = maxScore;
			resultDocids[k] = docids[maxRank];
			resultDocnos[k] = docnos[maxRank];
			
			k++;
			// mark as selected
			selected.put(docids[maxRank]);
			if (sim) {
				float[] sims = similarities(maxRank, docsContent[maxRank]);
				sims = normalize(sims);
				scorer.update(sims);				
			} else {
				
				float[] sims = similaritiesFromAspects(maxRank);
				sims = normalize(sims);
				
				scorer.update(sims);
			}

		}
		
		for (int i = depth; i < n; i++) {
			scores[i] = (1-lambda) * relevance[i];
		}
		
		QueryResultSet finalResultSet = new QueryResultSet();
		
		finalResultSet.setDocids(resultDocids);
		finalResultSet.setScores(resultScores);
		finalResultSet.setDocnos(resultDocnos);
		
		return finalResultSet;
	}
	
	private float[] similaritiesFromAspects(int maxRank) {
		float[] sims = new float[AspectCache.n];
		Arrays.fill(sims, 0);
		
		if (AspectCache.coverage == null){
			return sims;
		}
		
		for (int i = 0; i < sims.length; i++) {
			
			sims[i] = cosine(AspectCache.coverage[maxRank],AspectCache.coverage[i]);
		}
		
		return sims;
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

	@Override
	public ResultSet reranking(ResultSet baselineResultSet) {
		
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
