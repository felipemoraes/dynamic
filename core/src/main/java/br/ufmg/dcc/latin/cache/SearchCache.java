package br.ufmg.dcc.latin.cache;

import java.util.Map;

import org.apache.lucene.search.IndexSearcher;

import br.ufmg.dcc.latin.querying.CollectionResultSet;

public class SearchCache {
	
	public static String[] docsContent;
	public static int[] docids;
	public static float[] scores;
	public static String[] docnos;
	
	public static String indexName;	
	
	public static Map<String,IndexSearcher> indices;
	
	public static void cache(CollectionResultSet baselineResultSet) {
		docsContent = baselineResultSet.getDocsContent();
		docids = baselineResultSet.getDocids();
		scores = baselineResultSet.getScores();
		docnos = baselineResultSet.getDocnos();
		
	}
	
}
