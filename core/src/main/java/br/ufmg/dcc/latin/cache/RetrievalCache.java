package br.ufmg.dcc.latin.cache;

import java.util.Map;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

import br.ufmg.dcc.latin.querying.ResultSet;

public class RetrievalCache {
	
	
	public static int[] docids;
	public static float[] scores;
	public static String[] docnos;
	public static TopDocs topDocs;
	
	public static Map<String,IndexSearcher> indices;
	
	public static Map<String,float[]> passageCache;
	
	
	public static Map<String,float[]> subtopicsCache;
	
	public static void cache(ResultSet resultSet) {
		docids = resultSet.docids;
		scores = resultSet.scores;
		docnos = resultSet.docnos;
	}
	
}
