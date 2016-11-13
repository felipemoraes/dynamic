package br.ufmg.dcc.latin.searching;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;

import br.ufmg.dcc.latin.cache.SearchCache;

public class SearchEngineManager {
	
	private static QueryParser parser;
	
	private static Analyzer analyzer;
	 
	public static IndexSearcher getIndexSearcher(String indexName){
		if (SearchCache.indices == null) {
			SearchCache.indices = new HashMap<String,IndexSearcher>();
		}
		
		if (SearchCache.indices.containsKey(indexName)) {
			return SearchCache.indices.get(indexName);
		}
		
		IndexReader reader;
		IndexSearcher searcher = null;
		try {
			reader = DirectoryReader.open(FSDirectory.open( new File(indexName).toPath()) );
			searcher = new IndexSearcher(reader);
			SearchCache.indices.put(indexName, searcher);
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
}
