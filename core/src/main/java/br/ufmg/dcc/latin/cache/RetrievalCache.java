package br.ufmg.dcc.latin.cache;

import java.util.Map;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;

import br.ufmg.dcc.latin.querying.ResultSet;

public class RetrievalCache {
	
	
	public static int[] docids;
	public static double[] scores;
	public static String[] docnos;
	public static TopDocs topDocs;
	
	public static String topicId;
	
	public static Map<String,IndexSearcher> indices;
	
	public static Map<String,double[]> passageCache;
	
	
	public static Map<String,double[]> subtopicsCache;
	
	public static void cache(ResultSet resultSet) {
		docids = resultSet.docids;
		scores = resultSet.scores;
		docnos = resultSet.docnos;
	}
	
}
