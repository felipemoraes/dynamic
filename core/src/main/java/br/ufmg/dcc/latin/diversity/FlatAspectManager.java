package br.ufmg.dcc.latin.diversity;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.SearchCache;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.querying.SelectedSet;


public class FlatAspectManager implements AspectManager {
	
	
	private FlatAspectModel flatAspectModel;
	
	

	public FlatAspectManager(String[] docContent){
		try {
			AspectCache.indexWriter = new IndexWriter(AspectCache.indexDir, AspectCache.config);
		} catch (IOException e) {
			e.printStackTrace();
		}
		for (int i = 0; i < docContent.length; i++) {
			Document doc = new Document();
			doc.add(new TextField("content", docContent[i], Field.Store.YES));
			try {
				AspectCache.indexWriter.addDocument(doc);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			AspectCache.indexWriter.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		};
		
		AspectCache.n = docContent.length;
	}
	

	@Override
	public void miningDiversityAspects(Feedback[] feedbacks) {
		cacheFeedback(feedbacks);
		if (flatAspectModel == null) {
			flatAspectModel = new FlatAspectModel();
		}
		
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].isOnTopic()){
				continue;
			}
			Passage[] passages = feedbacks[i].getPassages();
			for (int j = 0; j < passages.length; j++) {
				flatAspectModel.addToAspect(passages[j].getAspectId(), passages[j].getText());
			}
		}
	
		int n = AspectCache.n;
		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			AspectCache.importance = new Aspect[0];
			AspectCache.novelty = new Aspect[0];
			AspectCache.coverage = new Aspect[0][];
			return;
		}
		AspectCache.importance = new Aspect[aspectSize];
		AspectCache.novelty = new Aspect[aspectSize];
		AspectCache.coverage = new Aspect[n][aspectSize];
		
		float uniformImportance = 1.0f/aspectSize;
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			AspectCache.importance[i] = new FlatAspect(uniformImportance);
			AspectCache.novelty[i] =  new FlatAspect(1.0f);
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
			    float[] scores = computeAspectSimilarity(aspectComponent);
			   
			    for(int j = 0;j< n ;++j) {
			    	if (AspectCache.coverage[j][i] == null ){
			    		AspectCache.coverage[j][i] = new FlatAspect(0);
			    	}
			    	
			    	float score = scores[j];
			    	if (AspectCache.coverage[j][i].getValue() < score) {
			    		AspectCache.coverage[j][i].setValue(score);
			    	}
			    }
			}
	
			for(int j = 0;j< n ;++j) {
				if (AspectCache.feedbacks[j] != null) {
					float score = AspectCache.feedbacks[j].getRelevanceAspect(aspectId);
					if (AspectCache.coverage[j][i] == null) {
						AspectCache.coverage[j][i] = new FlatAspect(0);
					}
					AspectCache.coverage[j][i].setValue(score);
				}
			}
			i++;
		}
		normalizeCoverage();
	}
	
	
	@Override
	public void miningProportionalAspects(Feedback[] feedbacks) {
		cacheFeedback(feedbacks);
		if (flatAspectModel == null) {
			flatAspectModel = new FlatAspectModel();
		}
		
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].isOnTopic()){
				continue;
			}
			Passage[] passages = feedbacks[i].getPassages();
			for (int j = 0; j < passages.length; j++) {
				flatAspectModel.addToAspect(passages[j].getAspectId(), passages[j].getText());
			}
		}
		
		int n = AspectCache.n;
		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			AspectCache.v = new Aspect[0];
			AspectCache.s = new Aspect[0];
			AspectCache.coverage = new Aspect[0][];
			return;
		}
		
		AspectCache.v = new Aspect[aspectSize];
		AspectCache.s = new Aspect[aspectSize];
		AspectCache.coverage = new Aspect[n][aspectSize];
		
		float uniformProportion = 1.0f/aspectSize;
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			AspectCache.v[i] = new FlatAspect(uniformProportion);
			AspectCache.s[i] =  new FlatAspect(1.0f);
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
			    float[] scores = computeAspectSimilarity(aspectComponent);
			   
			    for(int j = 0;j< n ;++j) {
			    	if (AspectCache.coverage[j][i] == null ){
			    		AspectCache.coverage[j][i] = new FlatAspect(0);
			    	}
			    	
			    	float score = scores[j];
			    	if (AspectCache.coverage[j][i].getValue() < score) {
			    		AspectCache.coverage[j][i].setValue(score);
			    	}
			    }
			}
	
			for(int j = 0;j< n ;++j) {
				if (AspectCache.feedbacks[j] != null) {
					float score = AspectCache.feedbacks[j].getRelevanceAspect(aspectId);
					if (AspectCache.coverage[j][i] == null) {
						AspectCache.coverage[j][i] = new FlatAspect(0);
					}
					AspectCache.coverage[j][i].setValue(score);
				}
			}
			i++;
		}
		normalizeCoverage();
		
	}
	
	
	public void cacheFeedback(Feedback[] feedbacks){
		if (AspectCache.feedbacks == null) {
			int n = SearchCache.docnos.length;
			AspectCache.feedbacks = new Feedback[n];
		}
		
		for (int i = 0; i < SearchCache.docnos.length; i++) {
			for (int j = 0; j < feedbacks.length; j++) {
				if (feedbacks[j].getDocno() == SearchCache.docnos[i]){
					AspectCache.feedbacks[i] = feedbacks[j]; 
				}
			}
		}
	}
	
	
	public void normalizeCoverage(){
		for (int i = 0; i < AspectCache.coverage[0].length; ++i) {
			float sum = 0;
			for (int j = 0; j < AspectCache.coverage.length; j++) {
				sum +=  AspectCache.coverage[j][i].getValue();
			}
			
			for (int j = 0; j < AspectCache.coverage.length; j++) {
				if (sum > 0) {
					float normValue = AspectCache.coverage[j][i].getValue()/sum;
					AspectCache.coverage[j][i].setValue(normValue);
				}
				
			}
		}
	}
	
	public void updateNovelty(SelectedSet selected){
		
		for (int j = 0; j < SearchCache.docids.length; ++j) {
			if (! selected.has(SearchCache.docids[j])) {
				continue;
			}
			for (int i = 0; i < AspectCache.novelty.length; i++) {
				float newNovelty = AspectCache.novelty[i].getValue()*(1-AspectCache.coverage[j][i].getValue());
				AspectCache.novelty[i].setValue(newNovelty);
			}
		}
		normalizeNovelty();
	}
	
	public void normalizeNovelty(){
		float sum = 0;
		for (int i = 0; i < AspectCache.novelty.length; i++) {
			sum += AspectCache.novelty[i].getValue();
		}
		for (int i = 0; i < AspectCache.novelty.length; i++) {
			if (sum > 0) {
				float normValue = AspectCache.novelty[i].getValue()/sum;
				AspectCache.novelty[i].setValue(normValue);
			}
		}
	}
	
	public void clear(){
		AspectCache.importance = null;
		AspectCache.novelty = null;
		AspectCache.coverage = null;
		AspectCache.feedbacks = null;
		flatAspectModel = null;
	}

	
	public float[] computeAspectSimilarity(String aspectComponent){
		
		IndexReader reader = null;
		try {
			reader = DirectoryReader.open(AspectCache.indexDir);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		IndexSearcher searcher  = new IndexSearcher(reader);
		
		BooleanQuery.setMaxClauseCount(200000);
		
	    int hitsPerPage = AspectCache.n;
	    float[] scores = new float[hitsPerPage];
	    
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
	    QueryParser queryParser = new QueryParser("content", AspectCache.analyzer);
	   
	    
	    try {
	    	 Query q = queryParser.parse(QueryParser.escape(aspectComponent).toLowerCase());
			searcher.search(q, collector);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	    
	    for(int i = 0;i<hits.length;++i) {
	    	int ix = hits[i].doc;
	    	scores[ix] = hits[i].score;
	    }

	    try {
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return scores;
		
	}


	public void printCoverage() {
		for (int i = 0; i < AspectCache.coverage.length; i++) {
			for (int j = 0; j < AspectCache.coverage[i].length; j++) {
				System.out.print(AspectCache.coverage[i][j].getValue() + " ");
			}
			System.out.println();
		}
		
	}
	
	public void printNovelty() {
		for (int i = 0; i < AspectCache.novelty.length; i++) {
			System.out.print(AspectCache.novelty[i].getValue() + " ");
		}
		System.out.println();
	}

}
