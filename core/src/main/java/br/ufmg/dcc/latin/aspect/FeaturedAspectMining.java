package br.ufmg.dcc.latin.aspect;

import java.util.Arrays;
import java.util.List;

import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;
import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FeaturedAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class FeaturedAspectMining  extends AspectMining {
	
	private double[] aspectWeights;
	
	public FeaturedAspectMining(double[] aspectWeights) {
		n = RetrievalCache.docids.length;
		importance = new double[0];
		novelty = new double[0];
		coverage = new double[n][0];
		s = new double[0];
		v = new double[0];
		accumulatedRelevance = new double[0];
		featuredAspectModel = new FeaturedAspectModel();
		ExternalKnowledgeCache.init();
	}
	
	private FeaturedAspectModel featuredAspectModel;
	
	@Override
	public void miningFeedback(String index, String query,  Feedback[] feedbacks) {
		
		cacheFeedback(feedbacks);
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].isOnTopic()){
				continue;
			}
			Passage[] passages = feedbacks[i].getPassages();
			for (int j = 0; j < passages.length; j++) {
				featuredAspectModel.addToAspect(passages[j].getAspectId(),passages[j].getPassageId(), passages[j].getRelevance());
			}
		}

		
		int aspectSize = featuredAspectModel.numAspects();
		if (aspectSize == 0) {
			return;
		}
		
		importance = new double[aspectSize];
		novelty = new double[aspectSize];
		coverage = new double[n][aspectSize];
		v = new double[aspectSize];
		s = new double[aspectSize];

		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		Arrays.fill(getV(), 1.0f);
		Arrays.fill(getS(), 1.0f);
		
		RetrievalController.loadDocFreqs(index);
		
		
		List<String> aspectsId = featuredAspectModel.getAspects();
		for (int i = 0; i < aspectsId.size(); ++i ) {
			String getAspectQuery = featuredAspectModel.getAspectQuery(aspectsId.get(i), aspectWeights );
			
			double[] scores = null;
			if (getAspectQuery.length() == 0) {
				scores = new double[n];
				Arrays.fill(scores, 1);
			} else {
				scores = RetrievalController.rerankResults(RetrievalCache.docids, index, getAspectQuery);
			}
			
		
			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }
		    
		    

			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					float score = this.feedbacks[j].getRelevanceAspect(aspectsId.get(i));
					coverage[j][i] = score;
				}
			}
		}
		normalizeCoverage();
		
	}
	
	
	@Override
	public void miningFeedbackForCube(String query, String index,  Feedback[] feedbacks) {
		
		cacheFeedback(feedbacks);
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].isOnTopic()){
				continue;
			}
			Passage[] passages = feedbacks[i].getPassages();
			for (int j = 0; j < passages.length; j++) {
				featuredAspectModel.addToAspect(passages[j].getAspectId(), passages[j].getPassageId(), passages[j].getRelevance());
			}
		}

		
		int aspectSize = featuredAspectModel.numAspects();
		if (aspectSize == 0) {
			return;
		}
		
		importance = new double[aspectSize];
		novelty = new double[aspectSize];
		coverage = new double[n][aspectSize];
		accumulatedRelevance = new double[aspectSize];

		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		Arrays.fill(getV(), 1.0f);
		Arrays.fill(getS(), 1.0f);
		
		RetrievalController.loadDocFreqs(index);
		
		
		List<String> aspectsId = featuredAspectModel.getAspects();
		for (int i = 0; i < aspectsId.size(); ++i ) {
			String getAspectQuery = featuredAspectModel.getAspectQuery(aspectsId.get(i), aspectWeights);
			
			double[] scores = null;
			if (getAspectQuery.length() == 0) {
				scores = new double[n];
				Arrays.fill(scores, 1);
			} else {
				scores = RetrievalController.rerankResults(RetrievalCache.docids, index, getAspectQuery);
			}
			
		
			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }
		    
		    

			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }

			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					
					float score = this.feedbacks[j].getRelevanceAspect(aspectsId.get(i));
					coverage[j][i] = score;
					
					if (score > 0) {
						novelty[i]++;
					}
					accumulatedRelevance[i] += score;
				}
			}
			
		}
		
		
	}
	
	
	

	public double[] getAspectWeights() {
		return aspectWeights;
	}

	public void setAspectWeights(double[] aspectWeights) {
		this.aspectWeights = aspectWeights;
	}


}
