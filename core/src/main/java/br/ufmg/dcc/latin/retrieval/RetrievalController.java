package br.ufmg.dcc.latin.retrieval;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.PostingsEnum;
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
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.search.similarities.LMDirichlet;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.rescoring.ReRankQueryRescorer;


public class RetrievalController {
	
	public static QueryParser parser;
	
	private static float[] fiedlWeights;
	
	private static Analyzer analyzer;
	
	private static Similarity similarity;
	
	public static Map<String,Terms[]> termsVector;
	
	public static Map<String, TermStatistics> termStatistics;
	
	private static Map<String, Integer> docCount;
	public static Map<String, Integer> sumTotalTerms;
	
	 
	public static IndexSearcher getIndexSearcher(String indexName){
		if (similarity == null) {
			similarity = new LMDirichlet(2000f);
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
			System.out.println(indexName + " " + reader.numDocs());
	
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
			getAnalyzer();
		}
		Map<String,Float> boosts = new HashMap<String,Float>();
		boosts.put("title", fiedlWeights[0]);
		boosts.put("content", fiedlWeights[1]);
		parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer, boosts);
		return parser;
	}
	
	public static QueryParser getQueryParser(String field){
		if (parser != null) {
			return parser;
		}
		if (analyzer == null) {
			getAnalyzer();
		}
		Map<String,Float> boosts = new HashMap<String,Float>();
		boosts.put(field, 1f);
		parser = new MultiFieldQueryParser(new String[]{field}, analyzer, boosts);
		return parser;
	}

	/*public static Analyzer getAnalyzer() {
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
        return analyzer;
	}*/
	
	public static Analyzer getAnalyzer() {
        return analyzer = new EnglishAnalyzer();

	}
	
	
	public static float[] rerankResults(int[] docids, String index, String query, String field){
		
		int n = RetrievalCache.docids.length;
		float[] scores = new float[n];
		Map<Integer,Integer> mapId = new HashMap<Integer, Integer>();
		for (int i = 0; i < scores.length; i++) {
			mapId.put(RetrievalCache.docids[i], i);
		}
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(index);
		
		BooleanQuery.setMaxClauseCount(100000);
		QueryParser parser = getQueryParser(field);
		Query q = null;
		try {
			q = parser.parse(query);
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
			e.printStackTrace();
		}

	    return scores;
	}
	
	
	
	public static float[] rerankResults(int[] docids, String index, String query){
		
		int n = RetrievalCache.docids.length;
		float[] scores = new float[n];
		Map<Integer,Integer> mapId = new HashMap<Integer, Integer>();
		for (int i = 0; i < scores.length; i++) {
			mapId.put(RetrievalCache.docids[i], i);
		}
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(index);
		
		BooleanQuery.setMaxClauseCount(1000000);
		QueryParser parser = getQueryParser();
		Query q = null;
		try {
			q = parser.parse(query);
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
	

	
	private static float tfidf(int docid1, int docid2, int docCount, TermStatistics termStatistics, String field){
		float score = 0;
		
		TermsEnum termVector1 = null;
		TermsEnum termVector2 = null;

		try {
			if (termsVector.get(field)[docid1] == null || termsVector.get(field)[docid2] == null ){
				return 0;
			}
			termVector1 = termsVector.get(field)[docid1].iterator();
			termVector2 = termsVector.get(field)[docid2].iterator();
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

	
				String t = term1.utf8ToString();
				float df = termStatistics.docFreq(t);
				float idf = (float)(Math.log(docCount)/(df+1));
				float weight1 = tf1*idf;
				float weight2 = tf2*idf;
				docNorm1 += weight1*weight1;
				docNorm2 += weight2*weight2;
				
				
				score += weight1*weight2;
				t1 = termVector1.next();
				t2 = termVector2.next();
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		docNorm1 = (float) Math.sqrt(docNorm1);
		docNorm2 = (float) Math.sqrt(docNorm2);


		score /= (docNorm1*docNorm2);
		
		return score;
	}
	
	public static float getIdf(String index, String field, String term){

		int count = docCount.get(index  + "_" + field );		
		float df = termStatistics.get(index  + "_" + field).docFreq(term);
			
		float idf = (float) (Math.log(count)/(df+1));
		return idf;
	}
	
	public static float[] getCosineSimilarities(int[] docids, int docid, String index, String field){
		
		int n = RetrievalCache.docids.length;
	
		
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(index);
		
		IndexReader reader = searcher.getIndexReader();
		
		loadDocFreqs(index);
		
		
		if (termsVector == null){
			termsVector = new HashMap<String,Terms[]>();
			Terms[] termsVectorContent = new Terms[n];
			Terms[] termsVectorTitle = new Terms[n];
			for (int i = 0; i < docids.length; i++) {
				try {
					termsVectorContent[i] = reader.getTermVector(docids[i], "content");
					termsVectorTitle[i] = reader.getTermVector(docids[i], "title");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			termsVector.put("content", termsVectorContent);
			termsVector.put("title", termsVectorTitle);
		}
		
		float[] scores = new float[n];
		int count = docCount.get(index +"_" + field);
		TermStatistics contentTermStatistics = termStatistics.get(index+ "_" + field);
		for (int i = 0; i < docids.length; i++) {
			scores[i] = tfidf(i,docid,count,contentTermStatistics,field);
		}
	    return scores;
	}

	public static void loadDocFreqs(String index) {
		
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(index);
		
		IndexReader reader = searcher.getIndexReader();
		
		
		if (termStatistics == null) {
	        
			termStatistics = new HashMap<String, TermStatistics>();
	        docCount = new HashMap<String,Integer>();
	        sumTotalTerms = new HashMap<String,Integer>();
		} 
		
		if (!termStatistics.containsKey(index + "_content")){
			
			try {
				BytesRef term = null;
				docCount.put(index + "_content", (int) searcher.collectionStatistics("content").docCount());
				docCount.put(index + "_title", (int) searcher.collectionStatistics("title").docCount());
				sumTotalTerms.put(index + "_content", (int) searcher.collectionStatistics("content").sumTotalTermFreq());
				sumTotalTerms.put(index + "_title", (int) searcher.collectionStatistics("title").sumTotalTermFreq());
				TermStatistics contentTermStatistics = new TermStatistics();
				TermsEnum termsEnum = MultiFields.getTerms(reader, "content").iterator();
		        while ((term = termsEnum.next()) != null) {
		        	contentTermStatistics.docFreq(term.utf8ToString(), (float) termsEnum.docFreq());
		        	contentTermStatistics.totalTermFreq(term.utf8ToString(), (float) termsEnum.totalTermFreq());
		        }
		        termStatistics.put(index + "_content", contentTermStatistics);
				TermStatistics titleTermStatistics = new TermStatistics();
				
				termsEnum = MultiFields.getTerms(reader, "title").iterator();
		        while ((term = termsEnum.next()) != null) {
		        	titleTermStatistics.docFreq(term.utf8ToString(), (float) termsEnum.docFreq());
		        	titleTermStatistics.totalTermFreq(term.utf8ToString(), (float) termsEnum.totalTermFreq());
		        }
		        termStatistics.put(index + "_title", titleTermStatistics);
				
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
		}
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
		RetrievalCache.topDocs = results;
		return resultSet;
	}

	public static void setSimilarity(Similarity sim) {
		similarity = sim;
	}

	public static float[] getFiedlWeights() {
		return fiedlWeights;
	}

	public static void setFiedlWeights(float[] fiedlWeights) {
		RetrievalController.fiedlWeights = fiedlWeights;
	}
}
