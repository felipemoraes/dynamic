package br.ufmg.dcc.latin.aspect;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import br.ufmg.dcc.latin.aspect.external.TermWeight;
import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class MostRelevantTermsAspectMining  extends AspectMining {

	private int maxTerms;
	
	public MostRelevantTermsAspectMining(int maxTerms) {
		n = RetrievalCache.docids.length;
		importance = new double[0];
		novelty = new double[0];
		coverage = new double[n][0];
		s = new double[0];
		v = new double[0];
		accumulatedRelevance = new double[0];
		flatAspectModel = new FlatAspectModel();
		this.maxTerms = maxTerms;
	}
	
	private FlatAspectModel flatAspectModel;
	
	@Override
	public void miningFeedback(String index, String query,  Feedback[] feedbacks) {
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
		
		importance = new double[aspectSize];
		novelty = new double[aspectSize];
		coverage = new double[n][aspectSize];
		v = new double[aspectSize];
		s = new double[aspectSize];
		features = new double[n][aspectSize][];

		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		Arrays.fill(getV(), 1.0f);
		Arrays.fill(getS(), 1.0f);
		
		RetrievalController.loadDocFreqs(index);
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			int s = flatAspectModel.getAspectComponents(aspectId).size();
			for(int j = 0;j< n ;++j) {
				features[j][i] = new double[s];
			}
			
			
			String aspectComponent = getComplexAspectComponent(query, index, flatAspectModel.getAspectComponentsAndWeights(aspectId));
			
			double[] scores = null;
			if (aspectComponent.length() == 0) {
				scores = new double[n];
				Arrays.fill(scores, 1);
			} else {
				scores = RetrievalController.rerankResults(RetrievalCache.docids, index, aspectComponent);
			}
			
		
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
	
	protected List<String> tokenizeString(Analyzer analyzer, String str) {
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
	
	private double getIdfFieldBased(String term, String index){
		
		float[] weights = RetrievalController.getFiedlWeights();
		double idf = weights[0]*RetrievalController.getIdf(index, "title", term) + weights[1]*RetrievalController.getIdf(index, "content", term);
		return idf;
	}

	private String getComplexAspectComponent(String query, String index, Map<String, List<Integer>> aspectComponentsAndWeights) {
		
		String complexAspectComponent = "";
		Analyzer analyzer = RetrievalController.getAnalyzer();
		
		Map<String,Double> termFreqs = new HashMap<String,Double>();

		for(Entry<String,List<Integer>> entry : aspectComponentsAndWeights.entrySet()){
			List<String> terms = tokenizeString(analyzer, entry.getKey());
			
			List<TermWeight> weights = computeTermWeights(terms,entry.getKey());
			
			float rel = 0;
			for (int relValue : entry.getValue()) {
				rel += relValue;
			}
			
			rel = rel/entry.getValue().size();
			
			if (rel <= 0) {
				continue;
			}
			
			for (TermWeight termWeight : weights) {
				if (!termFreqs.containsKey(termWeight.term)){
					termFreqs.put(termWeight.term, 0d);
				}
				termFreqs.put(termWeight.term, termFreqs.get(termWeight.term) + termWeight.weight*rel);
			}
			
		}
		
		
		
		double sum = 0;
	
		int querySize = Math.min(maxTerms, termFreqs.size());
		Map<String,Double> selectedTerms = new HashMap<String,Double>();
		while (selectedTerms.size() < querySize ) {
			double maxFreq = Double.NEGATIVE_INFINITY;
			String maxTerm = "-1";
			for (Entry<String,Double> termEntry : termFreqs.entrySet()) {
				if (selectedTerms.containsKey(termEntry.getKey())){
					continue;
				}
				
				if (maxFreq < termEntry.getValue()){
					maxFreq = termEntry.getValue();
					maxTerm = termEntry.getKey();
				}
			}
			selectedTerms.put(maxTerm,maxFreq);
			
			sum += maxFreq;
		}
		
		
		
		for (Entry<String,Double> term : selectedTerms.entrySet()) {
			double score = term.getValue()/sum;
			selectedTerms.put(term.getKey(), score);
		}
		
		/*
		Map<String,Float> queryLikelihood = new HashMap<String, Float>();
		
		for (String term : queryTerms) {
			if (!queryLikelihood.containsKey(term)) {
				queryLikelihood.put(term, 0f);
			}
			queryLikelihood.put(term, 1+ queryLikelihood.get(term));
		}
		
		float sumNormQuery = 0;
		for (String term : queryLikelihood.keySet()) {
			float tfidf = queryLikelihood.get(term)*getIdfFieldBased(term,index);
			queryLikelihood.put(term, queryLikelihood.get(term)*getIdfFieldBased(term,index));
			sumNormQuery += tfidf;
		}
		
		for (String term : queryLikelihood.keySet()) {
			queryLikelihood.put(term, queryLikelihood.get(term)/sumNormQuery);
		
		}
		
		
		for (String term : queryTerms) {
			if (!selectedTerms.containsKey(term)) {
				selectedTerms.put(term, queryLikelihood.get(term));
			} else {
				selectedTerms.put(term,selectedTerms.get(term) + queryLikelihood.get(term));
			}
		}
		*/
		
		for (Entry<String,Double> term : selectedTerms.entrySet()) {
			//complexAspectComponent += term.getKey() + String.format("^%.8f ", term.getValue());
			complexAspectComponent += term.getKey() + " ";
		}
		

		return complexAspectComponent;
	}

	protected List<TermWeight> computeTermWeights(List<String> terms, String passage) {
		System.out.println("This method need to be implemented by extension");
		return null;
	}

	@Override
	public void miningFeedbackForCube(String query, String index,Feedback[] feedbacks) {
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
		
		RetrievalController.loadDocFreqs(index);
		
		importance = new double[aspectSize];
		novelty = new double[aspectSize];
		coverage = new double[n][aspectSize];
		accumulatedRelevance = new double[aspectSize];
		
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		int i = 0;

		for (String aspectId : flatAspectModel.getAspects()) {
	
			
			String aspectComponent = getComplexAspectComponent(query, index, flatAspectModel.getAspectComponentsAndWeights(aspectId));
			
			double[] scores = null;
			
			if (aspectComponent.length() == 0) {
				scores = new double[n];
				Arrays.fill(scores, 1);
			} else {
				scores = RetrievalController.rerankResults(RetrievalCache.docids, index, aspectComponent);
			}
			
			
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
