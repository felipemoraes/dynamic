package br.ufmg.dcc.latin.models;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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
import br.ufmg.dcc.latin.search.elements.Subtopic;
import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.SearchResource;

public class ReRankerResource {
	
	
	
	public int[] docIds;
	public String[] docNos;
	public String[] docContent;
	public double[] relevance;
	public double[] importance;
	
	public double[][] coverageSuggestions;
	public double[][][] coverageHierarchicalSuggestions;
	
	private HashMap<String,Integer> subtopics;
	private HashMap<String,HashMap<String,Integer>> passages;
	
	private SearchResource searchResource;
	private Analyzer analyzer = new StandardAnalyzer();
	private Similarity similarity = new ClassicSimilarity();

	
	private Directory indexDir;
	private IndexWriter indexWriter;

	private Feedback[] feedbacks;
	private HashMap<String,Integer> mapIds;
	
	private int counter;
	private int counterTotalNonRelevant;
	private List<Integer> counterContiguousFeedback;
	
	private String stoppingRule;
	
	
	public void create(String index, String query, String stoppingRule) {
		if (searchResource == null) {
			searchResource = new SearchResource(index,"doc");
		} else if (!searchResource.getIndexName().equals(index)){
			searchResource.setIndexName(index);
		}

		String[] fields = {"text","title","anchor"};
		float[] weights = {0.6f,0.3f,0.1f};
		
		ResultSet resultSet = searchResource.search(query,fields,weights, 1000);
		
		
		subtopics = new HashMap<String,Integer>();
		passages = new HashMap<String,HashMap<String,Integer>>();
		
		docIds = resultSet.getDocIds();
		
		this.stoppingRule = stoppingRule;
		counter = 0;
		counterTotalNonRelevant = 0;
		counterContiguousFeedback = new ArrayList<Integer>();
		
		
		coverageSuggestions = null;
		coverageHierarchicalSuggestions = null;
		
		docNos = resultSet.getDocNos();
		
		docContent = resultSet.getDocContent();
		indexDir = new RAMDirectory();
	    IndexWriterConfig config = new IndexWriterConfig(analyzer);
	    config.setSimilarity(similarity);
	    try {
			indexWriter = new IndexWriter(indexDir, config);
			addDocs(docContent,docContent.length);
		    indexWriter.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int n = docNos.length;
		relevance = new double[n];
		for(int i = 0 ; i < n; ++i){
			relevance[i] = (double) resultSet.getScores()[i];
		}
		
		feedbacks = new Feedback[docIds.length];
		
		mapIds = new HashMap<String,Integer>();
		for (int i = 0; i < docNos.length; i++) {
			mapIds.put(docNos[i], i);
		}
	}
	
	
	private boolean fixedDepthStoppingRule(){
		if (counter >= 20) {
			return true;
		}
		return false;
	}
	
	private boolean totalNonRelevantStoppingRule(){
		if (counterTotalNonRelevant >= 30) {
			return true;
		}
		return false;
	}
	
	private boolean contiguousNonRelevantStoppingRule(){
		int count = 0;
		
		if ( counterContiguousFeedback.size() < 3){
			return false;
		}
		for (int i = counterContiguousFeedback.size() - 1; i >  counterContiguousFeedback.size() - 4; --i){
			count+=counterContiguousFeedback.get(i);
		}
		if (count >= 15) {
			return true;
		}
		return false;
	}
	
	
	public boolean stop(){
		if (stoppingRule.equals("SS1")){
			return fixedDepthStoppingRule();
		} else if (stoppingRule.equals("SS2")){
			return totalNonRelevantStoppingRule();
		} else if (stoppingRule.equals("SS3")){
			return contiguousNonRelevantStoppingRule();
		}
		return false;
	}
	
	public void updateRules(Feedback[] feedback){
		int counterNonRelevants = 0;
		for (int i = 0; i < feedback.length; i++) {
			if (!feedback[i].getOnTopic()){
				counterTotalNonRelevant++;
				counterNonRelevants++;
			}
		}
		counterContiguousFeedback.add(counterNonRelevants);
		counter++;
	}
	
	private void addDocs(String[] docContent, int depth) throws IOException {

		for (int i = 0; i < depth; i++) {
		  Document doc = new Document();
		  
		  doc.add(new TextField("content", docContent[i], Field.Store.YES));
		  indexWriter.addDocument(doc);
		  
		}
	}
	
	
	public double[][][] getHierarchicalCoverageWithSuggestions(String topicId, String suggestionsFile){
		if (coverageHierarchicalSuggestions != null){
			return coverageHierarchicalSuggestions;
		}
		HashMap<String,HashSet<String>> subtopics = new HashMap<String,HashSet<String>>(); 
		try (BufferedReader br = new BufferedReader(new FileReader(suggestionsFile))) {
			String line;
		    while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(";",4);
		    	if (!splitLine[0].equals(topicId)){
		    		continue;
		    	}
		    	
		    	if (splitLine.length > 2) {
		    		String firstLevelSubquery = splitLine[2];
			    	String secondLevelSubquery = splitLine[3];
			    	if (secondLevelSubquery.replace(" ", "").length() == 0) {
			    		continue;
			    	}
			    	if (!subtopics.containsKey(firstLevelSubquery)) {
			    		subtopics.put(firstLevelSubquery, new HashSet<String>());
			    	} 
			    	subtopics.get(firstLevelSubquery).add(secondLevelSubquery);
		    	}
		    	
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		double[][][] coverage = new double[docIds.length][subtopics.size()][];
		int i = 0;
		for (String firstLevel : subtopics.keySet()) {
			for (int j = 0; j < coverage.length; j++) {
				coverage[j][i] = new double[subtopics.get(firstLevel).size()];
			}
			int j = 0;
			
			for (String secondLevel : subtopics.get(firstLevel)) {
				
				double[] similarities = computeSimilarity(secondLevel);
				for (int k = 0; k < similarities.length; k++) {
					coverage[k][i][j] = similarities[k];
				}
				j++;
			}
			i++;
		}
		coverageHierarchicalSuggestions = coverage;
		return coverage;
	}
	public double[][] getCoverageWithSuggestions(String topicId, String suggestionsFile){
		if (coverageSuggestions != null) {
			return coverageSuggestions;
		}
		HashSet<String> subtopics = new HashSet<String>(); 
		try (BufferedReader br = new BufferedReader(new FileReader(suggestionsFile))) {
			String line;
		    while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(";",4);
		    	if (!splitLine[0].equals(topicId)){
		    		continue;
		    	}
		    	if (splitLine.length > 2) {
		    		String firstLevelSubquery = splitLine[2];
		    		subtopics.add(firstLevelSubquery);	
		    	}

		    	
		    	
		    }
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		double[][] coverage = new double[docIds.length][subtopics.size()];
		
		for (String firstLevel : subtopics) {
			int i = 0;
			double[] similarities = computeSimilarity(firstLevel);
			for (int k = 0; k < similarities.length; k++) {
				coverage[k][i] = similarities[k];
			}
			i++;
		}
		coverageSuggestions = coverage;
		return coverage;
	}

	double[] computeSimilarity(String subquery){
		double[] similarities = new double[docIds.length];
		Arrays.fill(similarities, 0);
		
		QueryParser queryParser = new QueryParser("content", analyzer);
		BooleanQuery.setMaxClauseCount(200000);
		
		
		try {
			Query q;
			q = queryParser.parse(QueryParser.escape(subquery).toLowerCase());
		    IndexReader reader = DirectoryReader.open(indexDir);
		    
		    IndexSearcher searcher = new IndexSearcher(reader);
		    searcher.setSimilarity(similarity);
		    int hitsPerPage = docIds.length;
		    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
		    
		    searcher.search(q, collector);
		    ScoreDoc[] hits = collector.topDocs().scoreDocs;
		    
		    
		    for(int i = 0;i<hits.length;++i) {
		    	int ix = hits[i].doc;
		    	similarities[ix] = hits[i].score;
		    }

		    reader.close();
		} catch (ParseException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

		return similarities;
	}
	
	
	public double[][][] getCoverageWithFeedback(Feedback[] feedback) {
		if (feedback == null) {
			return null;
		}
		for (int i = 0; i < feedback.length; i++) {
			int j = mapIds.get(feedback[i].getDocId());
			feedbacks[j] = feedback[i];
		}
		subtopics.clear();
		passages.clear();
		int si = 0;
		for (int i = 0; i < feedbacks.length; i++) {
			
			if (feedbacks[i] == null) {
				continue;
			}
			if (!feedbacks[i].getOnTopic()){
				continue;
			}
			for (Subtopic subtopic : feedbacks[i].getSubtopics()) {
				if (!subtopics.containsKey(subtopic.getId())) {
					subtopics.put(subtopic.getId(), si);
					si++;
				}
				if (!passages.containsKey(subtopic.getId())){
					passages.put(subtopic.getId(), new HashMap<String,Integer>());
					
				}
				int ix = passages.get(subtopic.getId()).size();
				if (!passages.get(subtopic.getId()).containsKey(subtopic.getPassageText())){
					passages.get(subtopic.getId()).put(subtopic.getPassageText(),ix);
					ix++;
				}
				
			}
		}
		
		int k = subtopics.size();
		int n = docIds.length;
		
		double[][][] coverage = new double[n][k][];
		for (String subtopic : subtopics.keySet()){
			int j = subtopics.get(subtopic);
			for (int i = 0; i < coverage.length; i++) {
				coverage[i][j] = new double[passages.get(subtopic).size()];
				Arrays.fill(coverage[i][j], 0);
			}
		}
		
		
		for (int i = 0; i < docIds.length; ++i){
			if (feedbacks[i] == null){
				continue;
			}
			
			for (int j = 0; j < k; j++) {
				Arrays.fill(coverage[i][j],0);
			}
			
			if (!feedbacks[i].getOnTopic()){
				continue;
			}
				
			for (Subtopic subtopic : feedbacks[i].getSubtopics()) {
				int j = subtopics.get(subtopic.getId());
				int l = passages.get(subtopic.getId()).get(subtopic.getPassageText());
				coverage[i][j][l] = (double) (subtopic.getRating()*2)/10;
				
			}

		}
		
		
		for (String subtopic : subtopics.keySet()){
			int j = subtopics.get(subtopic);
			for (String subquery : passages.get(subtopic).keySet() ) {
				k = passages.get(subtopic).get(subquery);
				double[] similarities = computeSimilarity(subquery);
				for (int i = 0; i < similarities.length; i++) {
					if (feedbacks[i] == null ){
						coverage[i][j][k] = similarities[i];
					}
				}
			
			}
		}
		
		
		return coverage;

	}
	
	public double[][][] updateAll(Feedback[] allFeedback) {
		
		
		int si = 0;
		for (int i = 0; i < allFeedback.length; i++) {
			if (allFeedback[i] == null) {
				continue;
			}
			if (!allFeedback[i].getOnTopic()){
				continue;
			}
			for (Subtopic subtopic : allFeedback[i].getSubtopics()) {
				if (!subtopics.containsKey(subtopic.getId())) {
					subtopics.put(subtopic.getId(), si);
					si++;
				}
				if (!passages.containsKey(subtopic.getId())){
					passages.put(subtopic.getId(), new HashMap<String,Integer>());
					
				}
				int ix = passages.get(subtopic.getId()).size();
				if (!passages.get(subtopic.getId()).containsKey(subtopic.getPassageText())){
					passages.get(subtopic.getId()).put(subtopic.getPassageText(),ix);
					ix++;
				}
				
			}
		}
		
		int k = subtopics.size();
		int n = docIds.length;
		
		double[][][] coverage = new double[n][k][];
		
		for (String subtopic : subtopics.keySet()){
			int j = subtopics.get(subtopic);
			for (int i = 0; i < coverage.length; i++) {
				coverage[i][j] = new double[passages.get(subtopic).size()];
				Arrays.fill(coverage[i][j], 0);
			}
		}
		
		
		for (String subtopic : subtopics.keySet()){
			int j = subtopics.get(subtopic);
			for (String subquery : passages.get(subtopic).keySet() ) {
				k = passages.get(subtopic).get(subquery);
				double[] similarities = computeSimilarity(subquery);
				for (int i = 0; i < similarities.length; i++) {
					coverage[i][j][k] = similarities[i];
					
				//	System.out.print(similarities[i] + " ");
				}
			//	System.out.println();
			}
		}
	
		return coverage;
	}
	
	
}
