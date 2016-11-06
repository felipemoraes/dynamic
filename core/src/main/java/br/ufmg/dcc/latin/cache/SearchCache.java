package br.ufmg.dcc.latin.cache;

import br.ufmg.dcc.latin.querying.CollectionResultSet;

public class SearchCache {
	
	public static String[] docsContent;
	public static int[] docids;
	public static float[] scores;
	public static String[] docnos;
	public static void cache(CollectionResultSet baselineResultSet) {
		docsContent = baselineResultSet.getDocsContent();
		docids = baselineResultSet.getDocids();
		scores = baselineResultSet.getScores();
		docnos = baselineResultSet.getDocnos();
		
	}
	
}
