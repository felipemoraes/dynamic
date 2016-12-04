package br.ufmg.dcc.latin.aspect;

import java.util.Arrays;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.dynamicsystem.TrecUser;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;

public class OracleAspectMining extends AspectMining {

	private FlatAspectModel flatAspectModel;
	
	public OracleAspectMining() {
		n = RetrievalCache.docids.length;
		importance = new double[0];
		novelty = new double[0];
		coverage = new double[n][0];
		v = new double[0];
		s = new double[0];
		accumulatedRelevance = new double[0];
		flatAspectModel = new FlatAspectModel();
	}
	
	@Override
	public void miningFeedback(String index, String query, Feedback[] feedbacks) {
		
		cacheFeedback(feedbacks);
		
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
		importance = new double[aspectSize];
		novelty = new double[aspectSize];
		coverage = new double[n][aspectSize];
		v = new double[aspectSize];
		s = new double[aspectSize];
		features = new double[n][aspectSize][];

		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		Arrays.fill(v, 1.0f);
		Arrays.fill(s, 1.0f);
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			
			double[] scores = null;
			
			if (RetrievalCache.subtopicsCache.containsKey(aspectId)) {
				scores = RetrievalCache.subtopicsCache.get(aspectId);
			} else {
				scores = TrecUser.get(RetrievalCache.docnos, RetrievalCache.topicId, aspectId);
				RetrievalCache.subtopicsCache.put(aspectId, scores);
			}
			
			for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }
			i++;

		}
		normalizeCoverage();

	}

	@Override
	public void miningFeedbackForCube(String index, String query, Feedback[] feedbacks) {
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
	
			
			double[] scores = null;
			if (RetrievalCache.subtopicsCache.containsKey(aspectId)) {
				scores = RetrievalCache.subtopicsCache.get(aspectId);
			} else {
				scores = TrecUser.get(RetrievalCache.docnos, RetrievalCache.topicId, aspectId);
				RetrievalCache.subtopicsCache.put(aspectId, scores);
			}
			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }
			i++;
		}

	}

}
