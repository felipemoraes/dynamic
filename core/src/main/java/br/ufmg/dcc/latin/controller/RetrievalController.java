package br.ufmg.dcc.latin.controller;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Rescorer;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DPHSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.rescoring.ReRankQueryRescorer;


public class RetrievalController {
	
	private static QueryParser parser;
	
	private static Analyzer analyzer;
	
	private static Similarity similarity;
	 
	public static IndexSearcher getIndexSearcher(String indexName){
		if (similarity == null) {
			similarity = new DPHSimilarity();
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
			reader = DirectoryReader.open(FSDirectory.open( new File("../etc/indices/" + indexName).toPath()) );
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
		Map<Integer,Integer> mapId = new HashMap<Integer, Integer>();
		for (int i = 0; i < scores.length; i++) {
			mapId.put(RetrievalCache.docids[i], i);
		}
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(RetrievalCache.indexName);
		
		BooleanQuery.setMaxClauseCount(400000);
		QueryParser parser = getQueryParser();
		Query q = null;
		try {
			q = parser.parse(QueryParser.escape(query));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		//searcher.setSimilarity(new BM25Similarity());
		Rescorer reRankQueryRescorer = new ReRankQueryRescorer(q, 1.0f);
		
	    try {
			TopDocs rescoredDocs = reRankQueryRescorer
			        .rescore(searcher, RetrievalCache.topDocs, 1000);
			ScoreDoc[] reRankScoreDocs = rescoredDocs.scoreDocs;
			for (int i = 0; i < reRankScoreDocs.length; i++) {
				int ix = mapId.get(reRankScoreDocs[i].doc);
				scores[ix] = reRankScoreDocs[i].score;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
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
	             docnos[i] = doc.get("docno");
/*	             int doclen = 0;
	             TokenStream stream  = analyzer.tokenStream(null, new StringReader(doc.get("content")));
	             stream.reset();
	             while (stream.incrementToken()) {
	            	 doclen++;
	             }
	             stream.close();
	             System.out.println(doclen);*/
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
	
		RetrievalCache.docids = docids;
		RetrievalCache.scores = scores;
		RetrievalCache.docnos = docnos;
		RetrievalCache.docsContent = docsContent;
		RetrievalCache.indexName = index;
		RetrievalCache.topDocs = results;
		return resultSet;
	}
}
