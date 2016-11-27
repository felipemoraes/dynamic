package br.ufmg.dcc.latin.aspect;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class MostRelevantTermsAspectMining  extends AspectMining {

	
	public MostRelevantTermsAspectMining() {
		n = RetrievalCache.docids.length;
		importance = new float[0];
		novelty = new float[0];
		coverage = new float[n][0];
		s = new float[0];
		v = new float[0];
		accumulatedRelevance = new float[0];
		flatAspectModel = new FlatAspectModel();
	}
	
	private FlatAspectModel flatAspectModel;
	
	@Override
	public void miningFeedback(Feedback[] feedbacks) {
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
				flatAspectModel.addToAspect(passages[j].getAspectId(), passages[j].getText(), passages[j].getRelevance());
			}
		}

		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			return;
		}
		
		importance = new float[aspectSize];
		novelty = new float[aspectSize];
		coverage = new float[n][aspectSize];
		v = new float[aspectSize];
		s = new float[aspectSize];
		features = new float[n][aspectSize][];

		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		Arrays.fill(getV(), 1.0f);
		Arrays.fill(getS(), 1.0f);
		
		RetrievalController.loadDocFreqs();
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			int s = flatAspectModel.getAspectComponents(aspectId).size();
			for(int j = 0;j< n ;++j) {
				features[j][i] = new float[s];
			}
			
			
			String aspectComponent = getComplexAspectComponent(RetrievalCache.query, flatAspectModel.getAspectComponentsAndWeights(aspectId));
			
			float[] scores = RetrievalController.getSimilaritiesRerank(RetrievalCache.docids, aspectComponent);
			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }

			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					float score = this.feedbacks[j].getRelevanceAspect(aspectId);
					coverage[j][i] = score;
				}
			}
			i++;
		}
		normalizeCoverage();
		
	}
	
	public static List<String> tokenizeString(Analyzer analyzer, String str) {
		List<String> result = new ArrayList<String>();
		try {
		      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
		      stream.reset();
	
		      while (stream.incrementToken()) {
		    	  result.add(stream.getAttribute(CharTermAttribute.class).toString());
		      } 
		      
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result;
		
	}

	private String getComplexAspectComponent(String query, Map<String, List<Integer>> aspectComponentsAndWeights) {
		String complexAspectComponent = "";
		Analyzer analyzer = RetrievalController.getAnalyzer();
		Map<String,Float> termFreqs = new HashMap<String,Float>();
		List<String> queryTerms = tokenizeString(analyzer, query);
		
		for (String term : queryTerms) {
			termFreqs.put(term,1f);
		}
		for(Entry<String,List<Integer>> entry : aspectComponentsAndWeights.entrySet()){
			List<String> terms = tokenizeString(analyzer, entry.getKey());
			for (String term : terms) {
				if (!termFreqs.containsKey(term)){
					termFreqs.put(term, 0f);
				}
				termFreqs.put(term, termFreqs.get(term) +  entry.getValue().size());
			}
		}
		
		float sum = 0;
		for (Entry<String,Float> termEntry : termFreqs.entrySet()) {
			float tfidf = termEntry.getValue()*RetrievalController.getIdf(termEntry.getKey());
			termFreqs.put(termEntry.getKey(), tfidf);
			sum += tfidf;
		}
		
		int querySize = Math.min(20, termFreqs.size());
		HashSet<String> selected = new HashSet<String>();
		while (selected.size() < querySize ) {
			float maxFreq = Float.NEGATIVE_INFINITY;
			String maxTerm = "-1";
			for (Entry<String,Float> termEntry : termFreqs.entrySet()) {
				if (selected.contains(termEntry.getKey())){
					continue;
				}
				if (maxFreq < termEntry.getValue()/sum){
					maxFreq = termEntry.getValue()/sum;
					maxTerm = termEntry.getKey();
				}
			}
			selected.add(maxTerm);
			complexAspectComponent += maxTerm + " ";
			//complexAspectComponent += maxTerm + " ";
			
		}
		return complexAspectComponent;
	}

	@Override
	public void miningFeedbackForCube(Feedback[] feedbacks) {
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
				flatAspectModel.addToAspect(passages[j].getAspectId(), passages[j].getText(),passages[j].getRelevance());
			}
		}

		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			return;
		}
		
		RetrievalController.loadDocFreqs();
		
		importance = new float[aspectSize];
		novelty = new float[aspectSize];
		coverage = new float[n][aspectSize];
		accumulatedRelevance = new float[aspectSize];
		
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		int i = 0;

		for (String aspectId : flatAspectModel.getAspects()) {
	
			
			String aspectComponent = getComplexAspectComponent(RetrievalCache.query, flatAspectModel.getAspectComponentsAndWeights(aspectId));
			float[] scores = RetrievalController.getSimilaritiesRerank(RetrievalCache.docids, aspectComponent);
			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }

			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					
					float score = this.feedbacks[j].getRelevanceAspect(aspectId);
					coverage[j][i] = score;
					
					if (score > 0) {
						novelty[i]++;
					}
					accumulatedRelevance[i] += score;
				}
			}
			i++;
		}
		
	}


}
