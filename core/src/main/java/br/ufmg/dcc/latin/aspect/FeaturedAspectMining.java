package br.ufmg.dcc.latin.aspect;

import java.util.Arrays;
import java.util.List;

import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;
import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FeaturedAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.ReScorerController;
import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class FeaturedAspectMining  extends AspectMining {
	
	public FeaturedAspectMining() {
		
		n = RetrievalCache.docids.length;
		importance = new double[20];
		novelty = new double[20];
		coverage = new double[n][20];
		s = new double[20];
		v = new double[20];
		accumulatedRelevance = new double[20];
		featuredAspectModel = new FeaturedAspectModel();
		ExternalKnowledgeCache.init();
	}
	
	private FeaturedAspectModel featuredAspectModel;
	
	@Override
	public void sendFeedback(String index, String query,  Feedback[] feedbacks) {
		
		cacheFeedback(feedbacks);
		
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].onTopic){
				continue;
			}
			Passage[] passages = feedbacks[i].passages;
			for (int j = 0; j < passages.length; j++) {
				featuredAspectModel.addToAspect(passages[j].aspectId,passages[j].passageId, passages[j].relevance);
			}
		}
		
	}
	
	
	@Override
	public void miningFeedbackForCube(String query, String index,  Feedback[] feedbacks) {
		
		cacheFeedback(feedbacks);
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].onTopic){
				continue;
			}
			Passage[] passages = feedbacks[i].passages;
			for (int j = 0; j < passages.length; j++) {
				featuredAspectModel.addToAspect(passages[j].aspectId, passages[j].passageId, passages[j].relevance);
			}
		}

		
		int aspectSize = featuredAspectModel.numAspects();
		if (aspectSize == 0) {
			return;
		}
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		Arrays.fill(v, 1.0f);
		Arrays.fill(s, 1.0f);
		
		List<String> aspectsId = featuredAspectModel.getAspects();
		for (int i = 0; i < aspectsId.size(); ++i ) {
	
			TIntDoubleHashMap getAspectQuery = featuredAspectModel.getAspectQuery(aspectsId.get(i), aspectWeights);
			double[] scores = null;
			if (getAspectQuery.size() == 0) {
				scores = new double[n];
				Arrays.fill(scores, 1);
			} else {
				scores =  ReScorerController.rescore(getAspectQuery);
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
	
	public void debug(){
		List<String> aspectsId = featuredAspectModel.getAspects();
		for (int i = 0; i < aspectsId.size(); ++i ) {
			System.out.print(aspectsId.get(i) + ": ");
			TIntDoubleHashMap getAspectQuery = featuredAspectModel.getAspectQuery(aspectsId.get(i), aspectWeights);
			int[] terms = getAspectQuery.keys();
			for (int j = 0; j < terms.length; j++) {
				System.out.print(RetrievalController.vocab[0].getTerm(terms[j]) + "^" + getAspectQuery.get(terms[j]) + " ");
			}
			System.out.println();
		}
	}


	@Override
	public void updateAspects(String index) {
		int aspectSize = featuredAspectModel.numAspects();
		if (aspectSize == 0) {
			return;
		}
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		Arrays.fill(v, 1.0f);
		Arrays.fill(s, 1.0f);

		List<String> aspectsId = featuredAspectModel.getAspects();
		for (int i = 0; i < aspectsId.size(); ++i ) {
			TIntDoubleHashMap getAspectQuery = featuredAspectModel.getAspectQuery(aspectsId.get(i), aspectWeights);
			double[] scores = null;
			if (getAspectQuery.size() == 0) {
				scores = new double[n];
				Arrays.fill(scores, 1);
			} else {
				scores =  ReScorerController.rescore(getAspectQuery);
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

}
