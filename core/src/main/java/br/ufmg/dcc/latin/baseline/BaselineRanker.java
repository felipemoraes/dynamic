package br.ufmg.dcc.latin.baseline;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.stat.StatUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.querying.BooleanSelectedSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.simulation.SimAP;


public class BaselineRanker {
	
	public QueryParser parser;
	
	public double[] fiedlWeights;
	
	private Analyzer analyzer;
	
	private  Similarity similarity;
	
	private static BaselineRanker baselineRanker;
	
	
	
	private ResultSet currentResultSet;
	
	private ResultSet resultSet;
	
	private double sensitivity;
	
	public ResultSet search(){
		
		
		currentResultSet.scores = SimAP.apply(resultSet.docnos, resultSet.scores);
		//currentResultSet.scores = resultSet.scores;
		double[] sortedScores = new double[resultSet.docnos.length];
		String[] sortedDocNos = new String[resultSet.docnos.length];
		BooleanSelectedSet selected = new BooleanSelectedSet(resultSet.docnos.length);
		for (int i = 0; i < resultSet.docnos.length; i++) {
			double bestScore = Double.NEGATIVE_INFINITY;
			int best = -1;
			for (int j = 0; j < resultSet.docnos.length; j++) {
				if (selected.has(j)) {
					continue;
				}
				if (currentResultSet.scores[j] > bestScore) {
					best = j;
					bestScore = currentResultSet.scores[j];
				}
			}
			sortedDocNos[i] = resultSet.docnos[best];
			sortedScores[i] = currentResultSet.scores[best];
			selected.put(best);
			
		}
		currentResultSet.scores = sortedScores;
		currentResultSet.docnos = sortedDocNos;
		return currentResultSet;
	}
	
	
	public ResultSet search(double epsilon){
		
		
		LaplaceDistribution dist = new LaplaceDistribution(0,sensitivity/epsilon);
		
		for (int i = 0; i < resultSet.docnos.length; i++) {
			currentResultSet.scores[i]  += dist.sample();
		}
		double min = StatUtils.min(currentResultSet.scores);
		if (min < 0) {
			min *= -1;
			for (int i = 0; i < resultSet.docnos.length; i++) {
				currentResultSet.scores[i] += + min;
			}
		}
		
		//currentResultSet.scores = resultSet.scores;
		double[] sortedScores = new double[resultSet.docnos.length];
		String[] sortedDocNos = new String[resultSet.docnos.length];
		BooleanSelectedSet selected = new BooleanSelectedSet(resultSet.docnos.length);
		for (int i = 0; i < resultSet.docnos.length; i++) {
			double bestScore = Double.NEGATIVE_INFINITY;
			int best = -1;
			for (int j = 0; j < resultSet.docnos.length; j++) {
				if (selected.has(j)) {
					continue;
				}
				if (currentResultSet.scores[j] > bestScore) {
					best = j;
					bestScore = currentResultSet.scores[j];
				}
			}
			sortedDocNos[i] = resultSet.docnos[best];
			sortedScores[i] = currentResultSet.scores[best];
			selected.put(best);
			
		}
		currentResultSet.scores = sortedScores;
		currentResultSet.docnos = sortedDocNos;
		return currentResultSet;
	}
	
	public double[] getBins(){
		return SimAP.getBins(resultSet.docnos);
	}
	

	
	public static BaselineRanker getInstance(Similarity similarity, double[] fiedlWeights){
		if (baselineRanker != null) {
			baselineRanker.fiedlWeights = fiedlWeights;
			baselineRanker.similarity = similarity;	
		} else {
			baselineRanker = new BaselineRanker(similarity, fiedlWeights);
		}
		return baselineRanker;
	}
	
	private BaselineRanker(Similarity similarity, double[] fiedlWeights){
		this.similarity = similarity;
		this.fiedlWeights = fiedlWeights;
	}
			
	
	private IndexSearcher getIndexSearcher(String indexName){
		
		IndexReader reader;
		IndexSearcher searcher = null;

		try {
			
			reader = DirectoryReader.open(FSDirectory.open( new File("../etc/indices/" + indexName).toPath()) );
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity(similarity);
			return searcher;
		} catch (IOException e) {
			e.printStackTrace();
		}
			
		return searcher;
		
	}
	
	public QueryParser getQueryParser(){
		if (parser != null) {
			return parser;
		}
		if (analyzer == null) {
			getAnalyzer();
		}
		Map<String,Float> boosts = new HashMap<String,Float>();
		boosts.put("title", (float) fiedlWeights[0]);
		boosts.put("content", (float) fiedlWeights[1]);
		parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer, boosts);
		return parser;
	}

	
	public Analyzer getAnalyzer() {
        return analyzer = new EnglishAnalyzer();
	}
	
	

	
	
	public List<String> tokenizeText(String text){
		List<String> result = new ArrayList<String>();
		try {
		      TokenStream stream  = analyzer.tokenStream(null, new StringReader(text));
		      stream.reset();
		      
		      while (stream.incrementToken()) {
		    	  BytesRef term = new BytesRef(stream.getAttribute(CharTermAttribute.class).toString());
		    	  result.add(term.utf8ToString());
		      } 
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result;
	}
	
	
	
	public ResultSet search(String queryTerms, String index) {
		
		int size = 1000;
		
		IndexSearcher searcher = getIndexSearcher(index);
		QueryParser parser = getQueryParser();
		
		ScoreDoc[] hits = null;
		
		TopDocs results = null;
		
		Query query = null;
		try {
			query = parser.parse(QueryParser.escape(queryTerms));

			results = searcher.search(query, size);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	
		
        hits = results.scoreDocs;
        int n = Math.min(size, hits.length);
        
    	int[] docids = new int[n];
    	String[] docnos = new String[n];
   
    	double[] scores = new double[n];
    	
    	
        for(int i=0; i< n; i++){
			try {
				 Document doc = searcher.doc(hits[i].doc);
	             docnos[i] = doc.get("docno");
	             scores[i] = hits[i].score;
	             docids[i] = hits[i].doc;
	             
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
       
        
		ResultSet resultSet = new ResultSet();
		this.resultSet = new ResultSet();
		resultSet.docids = docids;
		resultSet.scores = scores;
		resultSet.docnos = docnos;
		
		currentResultSet = resultSet;
		this.resultSet.docids = docids;
		this.resultSet.scores = scores;
		this.resultSet.docnos = docnos;
		
		
		double min = StatUtils.min(scores);
		double max = StatUtils.max(scores);
		double deltaF = max - min;
		sensitivity = deltaF;
		return resultSet;
	}

	public ResultSet search(double frac, double[] relevances) {
		Random random = new Random();
		int countIrr = 0;
		boolean[] stays = new boolean[relevances.length];
		Arrays.fill(stays, false);
		TreeList<Integer> irrelevants = new TreeList<Integer>();
		
		for (int i = 0; i < relevances.length; i++) {
			if (i >= 50) {
				stays[i] = true;
				continue;
			}
			
			if (relevances[i] == 0 ){
				countIrr++;
				irrelevants.add(i);
			} else {
				stays[i] = true;
			}
		
		}
		
		int removeIrr = (int) (countIrr*frac);
		for (int i = 0; i < removeIrr; i++) {
			int remove = random.nextInt(irrelevants.size());
			irrelevants.remove(remove);
		}
		
		for (int i = 0; i < irrelevants.size() ; i++) {
			stays[i] = true;
		}
		
		ResultSet resultSet = new ResultSet(relevances.length - removeIrr);
		int j = 0;
		
		for (int i = 0; i < stays.length; i++) {
			if (stays[i]) {
				resultSet.docids[j] = this.resultSet.docids[i];
				resultSet.docnos[j] = this.resultSet.docnos[i];
				resultSet.scores[j] = this.resultSet.scores[i];
				j++;
			} 
		}
	
		
		return resultSet;
	}

}
