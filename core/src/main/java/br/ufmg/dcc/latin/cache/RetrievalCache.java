package br.ufmg.dcc.latin.cache;

import java.util.Map;

import org.apache.lucene.search.IndexSearcher;

import br.ufmg.dcc.latin.diversity.FeaturedAspect;
import br.ufmg.dcc.latin.index.DocVec;
import br.ufmg.dcc.latin.index.InMemoryDirectedIndex;
import br.ufmg.dcc.latin.querying.ResultSet;

public class RetrievalCache {
	
	
	public static int[] docids;
	public static double[] scores;
	public static String[] docnos;
	
	public static String topicId;
	public static IndexSearcher index;
	public static String indexName;
	
	public static Map<String,double[]> passageCache;
	
	
	public static Map<String,double[]> subtopicsCache;
	
	public static Map<String, ResultSet> resultSetCache;
	
	public static Map<String, InMemoryDirectedIndex[]> directedIndexCache;
	
	public static void cache(ResultSet resultSet) {
		docids = resultSet.docids;
		scores = resultSet.scores;
		docnos = resultSet.docnos;
	}
	
	public static Map<String,FeaturedAspect> featuredAspectCache;
	
}
