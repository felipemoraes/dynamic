package br.ufmg.dcc.latin.cache;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import br.ufmg.dcc.latin.diversity.Aspect;

public class AspectCache {
	
	public static Analyzer analyzer = new StandardAnalyzer();
	public static Similarity similarity = new ClassicSimilarity();
	
	public static IndexWriterConfig config = new IndexWriterConfig(analyzer);
	
	public static Directory indexDir = new RAMDirectory();;
	public static IndexWriter indexWriter = null;
	public static int n;
	public static Aspect[] importance;
	public static Aspect[] novelty;
	public static Aspect[][] coverage;

}
