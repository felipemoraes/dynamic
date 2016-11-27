package br.ufmg.dcc.latin.aspect;

import java.util.Arrays;

import org.apache.lucene.queryparser.classic.QueryParser;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class WeightedPassageAspectMining extends AspectMining {

	
	private FlatAspectModel flatAspectModel;
	
	
	public WeightedPassageAspectMining() {
		n = RetrievalCache.docids.length;
		importance = new float[0];
		novelty = new float[0];
		coverage = new float[n][0];
		s = new float[0];
		v = new float[0];
		accumulatedRelevance = new float[0];
		flatAspectModel = new FlatAspectModel();
	}

	@Override
	public void miningFeedback(String query, String index, Feedback[] feedbacks) {
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
				flatAspectModel.addToAspect(passages[j].getAspectId(), query + " " + passages[j].getText(), passages[j].getRelevance());
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
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			int s = flatAspectModel.getAspectComponents(aspectId).size();
			for(int j = 0;j< n ;++j) {
				features[j][i] = new float[s];
			}
			int k = 0;
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
				float[] scores = null;
				if (RetrievalCache.passageCache.containsKey(aspectComponent)) {
					scores = RetrievalCache.passageCache.get(aspectComponent);
				} else {
					scores = RetrievalController.rerankResults(RetrievalCache.docids, index , QueryParser.escape(aspectComponent));
					RetrievalCache.passageCache.put(aspectComponent, scores);
				}
				float passageWeight = flatAspectModel.getAspectWeight(aspectId, aspectComponent);
			    scores = scaling(scores);
			    for(int j = 0;j< n ;++j) {
			    	float score = scores[j]*passageWeight;
			    	features[j][i][k] = score;
			    	if (coverage[j][i] < score) {
			    		coverage[j][i] = score;
			    	}
			    }
			    k++;
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
				flatAspectModel.addToAspect(passages[j].getAspectId(), query + " " + passages[j].getText(),passages[j].getRelevance());
			}
		}

		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			return;
		}
		importance = new float[aspectSize];
		novelty = new float[aspectSize];
		coverage = new float[n][aspectSize];
		accumulatedRelevance = new float[aspectSize];
		features = new float[n][aspectSize][];
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		int i = 0;

		for (String aspectId : flatAspectModel.getAspects()) {
			
			int s = flatAspectModel.getAspectComponents(aspectId).size();
			for(int j = 0;j< n ;++j) {
				features[j][i] = new float[s];
			}
			int k = 0;
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
				float[] scores = null;
				if (RetrievalCache.passageCache.containsKey(aspectComponent)) {
					scores = RetrievalCache.passageCache.get(aspectComponent);
				} else {
					scores = RetrievalController.rerankResults(RetrievalCache.docids,  index, QueryParser.escape(aspectComponent));
					RetrievalCache.passageCache.put(aspectComponent, scores);
				}
				
			    scores = scaling(scores);
			    float passageWeight = flatAspectModel.getAspectWeight(aspectId, aspectComponent);
			    for(int j = 0;j< n ;++j) {
			    	float score = scores[j]*passageWeight;
			    	features[j][i][k] = score;
			    	if (coverage[j][i] < score) {
			    		coverage[j][i] = score;
			    	}
			    }
			    k++;
			}

			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					float score = this.feedbacks[j].getRelevanceAspect(aspectId);
					coverage[j][i] = score;
					
					if (score > 0) {
						novelty[i]+=1;
					}

					accumulatedRelevance[i] += score;
				}
			}
			
			i++;
		}
		
	}



}
