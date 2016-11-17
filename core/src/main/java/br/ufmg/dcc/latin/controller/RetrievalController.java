package br.ufmg.dcc.latin.controller;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexReaderContext;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermContext;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Rescorer;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.LMDirichlet;
import org.apache.lucene.search.similarities.Similarity;

import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.rescoring.ReRankQueryRescorer;


public class RetrievalController {
	
	private static QueryParser parser;
	
	private static Analyzer analyzer;
	
	private static Similarity similarity;
	
	private static DirectedIndexController directedIndexController;
	
	public static Terms[] termsVector;
	
	public static Map<String, Map<String,Float>> docFreqs;
	
	private static Map<String, Integer> docCount;
	 
	public static IndexSearcher getIndexSearcher(String indexName){
		if (similarity == null) {
			similarity = new LMDirichlet(2500.0f);
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
	
	public static float[] getSimilaritiesRerank(int[] docids, String query){
		
		int n = RetrievalCache.docids.length;
		float[] scores = new float[n];
		Map<Integer,Integer> mapId = new HashMap<Integer, Integer>();
		for (int i = 0; i < scores.length; i++) {
			mapId.put(RetrievalCache.docids[i], i);
		}
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(RetrievalCache.indexName);
		
		BooleanQuery.setMaxClauseCount(1000000);
		QueryParser parser = getQueryParser();
		Query q = null;
		try {
			q = parser.parse(QueryParser.escape(query.toLowerCase()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
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
	

	
	private static float tfidf(int docid1, int docid2, int docCount, Map<String,Float> docFreqs){
		float score = 0;
		
		TermsEnum termVector1 = null;
		TermsEnum termVector2 = null;

		try {
			termVector1 = termsVector[docid1].iterator();
			termVector2 = termsVector[docid2].iterator();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		float docNorm1 = 0;
		float docNorm2 = 0;
		try {

			BytesRef t1 = termVector1.next();
			BytesRef t2 = termVector2.next();

			while( t1 != null && t2!= null) {
				
				PostingsEnum p1 = termVector1.postings( null, PostingsEnum.ALL );
				p1.nextDoc();
				float tf1 = p1.freq();
				PostingsEnum p2 = termVector2.postings( null, PostingsEnum.ALL );
				p2.nextDoc();
				float tf2 = p2.freq();
				
				BytesRef term1 = termVector1.term();
				BytesRef term2 = termVector2.term();
				
				while(!term1.equals(term2)){
					if (term1.compareTo(term2) < 0){
						t1 = termVector1.next();
					} else if (term1.compareTo(term2) > 0) {
						t2 = termVector2.next();
					}
					if (t1 == null || t2 == null) {
						break;
					}
					term1 = termVector1.term();
					term2 = termVector2.term();
				}
				
				if (t1 == null || t2 == null) {
					break;
				}

				float df = 0;
				String t = term1.utf8ToString();
				if (docFreqs.containsKey(t)){
					df = (float) docFreqs.get(t);
					
				} 
				
				docNorm1 += tf1*tf1;
				docNorm2 += tf2*tf2;
				float idf = (float)(Math.log(docCount)/(df+1));
				
				score += tf1*idf*tf2*idf;
				t1 = termVector1.next();
				t2 = termVector2.next();
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		docNorm1 = (float) Math.sqrt(docNorm1);
		docNorm2 = (float) Math.sqrt(docNorm2);

		score /= docNorm1*docNorm2;
		
		return score;
	}
	
	public static float[] getSimilarities(int[] docids, int docid){
		
		int n = RetrievalCache.docids.length;
		
		
		
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(RetrievalCache.indexName);
		
		IndexReader reader = searcher.getIndexReader();
		
		if (docFreqs == null) {
	        
	        docFreqs = new HashMap<String, Map<String, Float> >();
	        docCount = new HashMap<String,Integer>();
		} 
		
		if (!docFreqs.containsKey(RetrievalCache.indexName)){
			Map<String,Float> docFreqIndex = new HashMap<String,Float>();
			try {
				BytesRef term = null;
				docCount.put(RetrievalCache.indexName, (int) searcher.collectionStatistics("content").docCount());
				TermsEnum termsEnum = MultiFields.getTerms(reader, "content").iterator();
		        while ((term = termsEnum.next()) != null) {
		        	docFreqIndex.put(term.utf8ToString(),(float) termsEnum.docFreq());
		        }
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			docFreqs.put(RetrievalCache.indexName, docFreqIndex);
		}
		
		
		if (termsVector == null){
			termsVector = new Terms[n];
			for (int i = 0; i < docids.length; i++) {
				try {
					termsVector[i] = reader.getTermVector(docids[i], "content");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		float[] scores = new float[n];
		int count = docCount.get(RetrievalCache.indexName);
		Map<String,Float> dfreq = docFreqs.get(RetrievalCache.indexName);
		for (int i = 0; i < docids.length; i++) {
			scores[i] = tfidf(i,docid,count,dfreq);
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

	public static void setSimilarity(Similarity sim) {
		similarity = sim;
	}
}
