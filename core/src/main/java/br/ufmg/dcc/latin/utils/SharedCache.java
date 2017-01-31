package br.ufmg.dcc.latin.utils;

import org.apache.lucene.index.IndexReader;

public class SharedCache {
	
	public static int[] docids;
	public static String[] docnos;
	public static double[] scores;
	
	static double[] fieldWeights;
	
	static InMemoryDirectedIndex[] directedIndex;
	
	static InMemoryVocabulary[] vocab;
	static InMemoryTermStats[] termStats;
	
	static IndexReader passageReader;
	
	
}
