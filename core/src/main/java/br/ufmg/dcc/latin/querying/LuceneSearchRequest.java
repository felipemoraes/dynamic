package br.ufmg.dcc.latin.querying;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import br.ufmg.dcc.latin.searching.SearchEngineManager;

public class LuceneSearchRequest implements SearchRequest {
	
	private static LuceneSearchRequest instance;
	
	public static LuceneSearchRequest getInstance(){
		if (instance == null) {
			instance = new LuceneSearchRequest();
		}
		return instance;
	}
	
	

	@Override
	public ResultSet search(QueryRequest queryTerms) {

		int size = queryTerms.getSize();
		
		IndexSearcher searcher = SearchEngineManager.getIndexSearcher(queryTerms.getIndex());
		QueryParser parser = SearchEngineManager.getQueryParser();
		
		ScoreDoc[] hits = null;
		
		TopDocs results = null;
		
		Query query = null;
		try {
			query = parser.parse(QueryParser.escape(queryTerms.getQuery()));
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
        
		CollectionResultSet resultSet = new CollectionResultSet();
		
		resultSet.setDocids(docids);
		resultSet.setScores(scores);
		resultSet.setDocnos(docnos);
		resultSet.setDocsContent(docsContent);
	
		return resultSet;
	}

}
