package br.ufmg.dcc.latin.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.querying.ResultSet;

public class RetrievalController {
	
	private static QueryParser parser;
	
	private static Analyzer analyzer;
	
	private static Similarity similarity;
	 
	public static IndexSearcher getIndexSearcher(String indexName){
		if (similarity == null) {
			similarity = new LMDirichletSimilarity(2500f);
		}
		if (RetrievalCache.indices == null) {
			RetrievalCache.indices = new HashMap<String,IndexSearcher>();
		}
		
		if (RetrievalCache.indices.containsKey(indexName)) {
			RetrievalCache.indices.get(indexName).setSimilarity(similarity);
			return RetrievalCache.indices.get(indexName);
		}
		
		IndexReader reader;
		IndexSearcher searcher = null;
		try {
			reader = DirectoryReader.open(FSDirectory.open( new File(indexName).toPath()) );
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(similarity);
			RetrievalCache.indices.put(indexName, searcher);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return searcher;
	}
	
	public static QueryParser getQueryParser(){
		if (parser != null) {
			return parser;
		}
		if (analyzer == null) {
			createAnalyzer();
		}
		Map<String,Float> boosts = new HashMap<String,Float>();
		boosts.put("title", 0.3f);
		boosts.put("content", 0.7f);
		parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer, boosts);
		return parser;
	}

	private static void createAnalyzer() {
        CustomAnalyzer.Builder builder = CustomAnalyzer.builder();
        try {
			builder.withTokenizer("standard");
	        builder.addTokenFilter("lowercase");
	        builder.addTokenFilter("stop");
	        builder.addTokenFilter("kstem");
		} catch (IOException e) {
			e.printStackTrace();
		}
        analyzer = builder.build();

	}
	
	public static float[] getSimilarities(int[] docids, String query, Similarity similarity){
		int n = RetrievalCache.docids.length;
		float[] scores = new float[n];
		
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(RetrievalCache.indexName);
		searcher.setSimilarity(similarity);
		BooleanQuery.setMaxClauseCount(200000);
	    QueryParser queryParser = RetrievalController.getQueryParser();
	 
	    try {
	    	Query q = queryParser.parse(QueryParser.escape(query));
	    	
	 	    for(int i = 0;i<n;++i) {
	 	    	Explanation exp = searcher.explain(q, RetrievalCache.docids[i]);
	 	    	scores[i] = exp.getValue();
	 	    }
	 	    
		} catch (IOException | ParseException  e) {
			e.printStackTrace();
		} 
	   
	    return scores;
	}
	
	public static ResultSet search(String queryTerms, String index) {

		int size = 1000;
		
		IndexSearcher searcher = getIndexSearcher(index);
		QueryParser parser = getQueryParser();
		
		ScoreDoc[] hits = null;
		
		TopDocs results = null;
		
		Query query = null;
		try {
			query = parser.parse(QueryParser.escape(queryTerms));
			results = searcher.search(query, size);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}

		
        hits = results.scoreDocs;
        int n = Math.min(size, hits.length);
        
    	int[] docids = new int[n];
    	String[] docnos = new String[n];
    	String[] docsContent = new String[n];
    	float[] scores = new float[n];
    	
        for(int i=0; i< n; i++){
			try {
				 Document doc = searcher.doc(hits[i].doc);
	             docnos[i] = doc.get("docno");;
	             scores[i] = hits[i].score;
	             docids[i] = hits[i].doc;
	             docsContent[i] = doc.get("content");
			} catch (IOException e) {
				e.printStackTrace();
			}

        }
        
		ResultSet resultSet = new ResultSet();
		
		resultSet.docids = docids;
		resultSet.scores = scores;
		resultSet.docnos = docnos;
		resultSet.docsContent = docsContent;
	
		return resultSet;
	}
}
