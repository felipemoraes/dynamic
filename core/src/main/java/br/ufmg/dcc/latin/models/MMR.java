package br.ufmg.dcc.latin.models;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.similarities.ClassicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;

import br.ufmg.dcc.latin.search.elements.Feedback;
import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.SearchResource;

public class MMR {
	

	private int[] docIds;
	private String[] docNos;
	private String[] docContent;
	private double[] relevance;
	
	private HashMap<Integer,Integer> mapIds;
	private Set<Integer> selected;
	private Directory indexDir;
	private IndexWriter indexWriter;
	private SearchResource searchResource;
	
	private static Analyzer analyzer = new StandardAnalyzer();
	private static Similarity similarity = new ClassicSimilarity();
	
	private double[] cacheSim;
	
	
	
	
	public void create(String index, String query) {
		if (searchResource == null) {
			searchResource = new SearchResource(index,"doc");
		} else if (!searchResource.getIndexName().equals(index)){
			searchResource.setIndexName(index);
		}
		
		String[] fields = {"text"};
		
		int depth = 50;
		
		
		
		ResultSet resultSet = searchResource.search(query,fields, 5000);
		System.out.println("Retrieved");
		docIds = resultSet.getDocIds();
		docNos = resultSet.getDocNos();
		docContent = resultSet.getDocContent();
		
		if (docIds.length < depth){
			depth = docContent.length;
		}
		
		relevance = new double[docIds.length];
		for (int i = 0; i < relevance.length; ++i){
			relevance[i] = (double) resultSet.getScores()[i];
		}
		
		mapIds = new HashMap<Integer,Integer>();
		selected = new HashSet<Integer>();
		
		for (int i = 0; i < docIds.length; ++i){
			mapIds.put(docIds[i], i);
		}
		cacheSim = new double[depth];
		Arrays.fill(cacheSim, 0.0);
		System.out.println("Indexing");
	    indexDir = new RAMDirectory();
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    config.setSimilarity(similarity);

	    try {
			indexWriter = new IndexWriter(indexDir, config);
			addDocs(docContent,depth);
			System.out.println("Indexed");
		    indexWriter.close();
		    System.out.println("Rescoring");
		    relevance = rescore(relevance, docIds, docContent,depth);

		    
		    System.out.println("Rescored");

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	double updateCache(int selected) throws ParseException, IOException{
		
		QueryParser queryParser = new QueryParser("content", analyzer);
		BooleanQuery.setMaxClauseCount(200000);
		
		Query q = queryParser.parse(QueryParser.escape(docContent[selected]).toLowerCase());
		
	    IndexReader reader = DirectoryReader.open(indexDir);
	    
	    IndexSearcher searcher = new IndexSearcher(reader);
	    searcher.setSimilarity(similarity);
	    int hitsPerPage = docIds.length;
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
	    
	    searcher.search(q, collector);
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	   
	    double maxScore = 0;
	    
	    for(int i = 0;i<hits.length;++i) {
	    	int cache_ix = hits[i].doc;
	    	if (cacheSim[cache_ix] < hits[i].score){
	    		cacheSim[cache_ix] = hits[i].score;
	    	}
	    }

	    reader.close();
	    
		return maxScore;
	}
	
	private double[] rescore(double[] relevance, int[] docIds, String[] docContents, int depth) throws ParseException, IOException{
		
		Set<Integer> selected = new HashSet<Integer>();
		int n = docIds.length;
		double[] scores = new double[n];
		double lambda = 0.3;
		double maxScore = Double.NEGATIVE_INFINITY;
		int maxRank = -1;
		
		while (selected.size() < depth) {
			maxScore = Double.NEGATIVE_INFINITY;
			maxRank = -1;
			// greedily select max document
			for (int i = 0; i < depth; ++i){
				if (selected.contains(i)) {
					continue;
				}
				
				double score = lambda*(relevance[i] - (1-lambda)*cacheSim[i]);
				
				if (score > maxScore){
					maxRank = i;
					maxScore = score;
				}
			}
			
			selected.add(maxRank);
			updateCache(maxRank);
			scores[maxRank] = maxScore;
		}
		
		for (int i = depth; i < n; ++i){
			scores[i] = maxScore*( (double) (n-i-+1)/(n-depth));
			
		}
		
		return scores;
		
	}

	public Map<String, Double> get() {
		
		Map<String, Double> result = new HashMap<String, Double>();
		
		while (result.size() < 5 && selected.size() < docIds.length){
			int maxRank = -1;
			double maxScore = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < relevance.length; i++) {
				if (selected.contains(i)){
					continue;
				}
				if (maxScore < relevance[i]) {
					maxRank = i;
					maxScore = relevance[i];
				}
			}
			selected.add(maxRank);
			result.put(docNos[maxRank], maxScore);
			
		}
		
		return result;
	}


	private void addDocs(String[] docContent, int depth) throws IOException {

		for (int i = 0; i < depth; i++) {
		  Document doc = new Document();
		  doc.add(new TextField("content", docContent[i], Field.Store.YES));
		  indexWriter.addDocument(doc);
		  
		}
		
	}

}
