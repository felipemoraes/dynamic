package br.ufmg.dcc.latin.aspect;

import java.util.Arrays;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.ReScorerController;
import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.map.hash.TIntDoubleHashMap;


public class PassageAspectMining extends AspectMining {

	private FlatAspectModel flatAspectModel;
	
	String query;

	public PassageAspectMining(){
		
		n = RetrievalCache.docids.length;
		importance = new double[0];
		novelty = new double[0];
		coverage = new double[n][0];
		v = new double[0];
		s = new double[0];
		
		hierarchicalImportance = new double[0][0];
		hierarchicalNovelty =  new double[0][0];
		hierarchicalCoverage = new double[n][0][0];
		
		hierarchicalS = new double[0][0];
		hierarchicalV = new double[0][0];
		accumulatedRelevance = new double[0];
		flatAspectModel = new FlatAspectModel();
	}
	
	
	@Override
	public void sendFeedback(String index, String query, Feedback[] feedbacks) {
		
		cacheFeedback(feedbacks);
		if (flatAspectModel == null) {
			flatAspectModel = new FlatAspectModel();
		}
		this.query = query;
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].onTopic){
				continue;
			}
			Passage[] passages = feedbacks[i].passages;
			for (int j = 0; j < passages.length; j++) {
				flatAspectModel.addToAspect(passages[j].aspectId, passages[j].passageId, query + " " + RetrievalController.getPassage(passages[j].passageId), passages[j].relevance);
			}
		}
	}

	@Override
	public void miningFeedbackForCube(String query, String index, Feedback[] feedbacks) {
		cacheFeedback(feedbacks);
		if (flatAspectModel == null) {
			flatAspectModel = new FlatAspectModel();
		}
		
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].onTopic){
				continue;
			}
			Passage[] passages = feedbacks[i].passages;
			for (int j = 0; j < passages.length; j++) {
				flatAspectModel.addToAspect(passages[j].aspectId, RetrievalController.getPassage(passages[j].passageId),passages[j].relevance);
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
		features = new double[n][aspectSize][];
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		int i = 0;

		for (String aspectId : flatAspectModel.getAspects()) {
			
			int s = flatAspectModel.getAspectComponents(aspectId).size();
			for(int j = 0;j< n ;++j) {
				features[j][i] = new double[s];
			}
			int k = 0;
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				aspectComponent = query + " " + aspectComponent;
				double[] scores = null;
				if (RetrievalCache.passageCache.containsKey(aspectComponent)) {
					scores = RetrievalCache.passageCache.get(aspectComponent);
				} else {
					TIntDoubleHashMap complexQuery = ReScorerController.getComplexQuery(query);
					scores = ReScorerController.rescore(complexQuery);
				}
				
			    scores = scaling(scores);
			    for(int j = 0;j< n ;++j) {
			    	double score = scores[j];
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


	@Override
	public void updateAspects(String index) {
		
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
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			int s = flatAspectModel.getAspectComponents(aspectId).size();
			for(int j = 0;j< n ;++j) {
				features[j][i] = new double[s];
			}
			int k = 0;
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
				double[] scores = null;
				
				if (RetrievalCache.passageCache.containsKey(aspectComponent)) {
					scores = RetrievalCache.passageCache.get(aspectComponent);
				} else {
					TIntDoubleHashMap complexQuery = ReScorerController.getComplexQuery(aspectComponent);
					scores = ReScorerController.rescore(complexQuery);
				}
				
			    scores = scaling(scores);
			    for(int j = 0;j< n ;++j) {
			    	double score = scores[j];
			    	features[j][i][k] = score;
			    	if (coverage[j][i] < score) {
			    		coverage[j][i] = score;
			    	}
			    }
			    k++;
			}
			k = 0;
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
	
				for(int j = 0;j< n ;++j) {
					if (this.feedbacks[j] != null) {
						float score = this.feedbacks[j].getRelevanceAspect(aspectId);
						coverage[j][i] = score;
					}
				}
			k++;
			}
			i++;
		}
		normalizeCoverage();
		
	}
	
	@Override
	public void updateHierarchicalAspects(String index) {
		int aspectSize = flatAspectModel.getAspects().size();
		
		if (aspectSize == 0) {
			return;
		}
		hierarchicalImportance = new double[aspectSize][0];
		hierarchicalNovelty = new double[aspectSize][0];
		hierarchicalCoverage = new double[n][aspectSize][0];
		v = new double[aspectSize];
		Arrays.fill(v, 1.0f);
		hierarchicalV = new double[aspectSize][0];
		hierarchicalS = new double[aspectSize][0];
		features = new double[n][aspectSize][0];

		
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {
			int s = flatAspectModel.getAspectComponents(aspectId).size();
			float uniformImportance = 1.0f/s;
			hierarchicalNovelty[i] = new double[s];
			hierarchicalImportance[i] = new double[s];
			
			hierarchicalS[i] = new double[s];
			hierarchicalV[i] = new double[s];
			
			Arrays.fill(hierarchicalNovelty[i], 1.0f);
			Arrays.fill(hierarchicalImportance[i], uniformImportance);
			
			Arrays.fill(hierarchicalS[i], 1.0f);
			Arrays.fill(hierarchicalV[i], 1.0f);
			
			
			for (int j = 0; j < hierarchicalCoverage.length; j++) {
				hierarchicalCoverage[j][i] = new double[s];
			}
			
			int k = 0;
			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
				double[] scores = null;
				if (RetrievalCache.passageCache.containsKey(aspectComponent)) {
					scores = RetrievalCache.passageCache.get(aspectComponent);
				} else {
					TIntDoubleHashMap complexQuery = ReScorerController.getComplexQuery(aspectComponent);
					scores = ReScorerController.rescore(complexQuery);
					for (int j = 0; j < scores.length; j++) {
						hierarchicalCoverage[j][i][k] = scores[j];
					}
				}
				
			    scores = scaling(scores);

			    k++;
			}
	
			k = 0;
			for (Integer passageId: flatAspectModel.getAspectComponentsIds(aspectId)) {
				for(int j = 0;j< n ;++j) {
					if (this.feedbacks[j] != null) {
						Passage[] passages = this.feedbacks[j].passages;
						if (passages == null) {
							hierarchicalCoverage[j][i][k] = 0;
							continue;
						}
						for (int l = 0; l < passages.length; l++) {
							if (passages[l].passageId == passageId){
								float score = this.feedbacks[j].getRelevanceAspect(aspectId,passageId);
								hierarchicalCoverage[j][i][k] = score;
							}
						}
					}
				}
				k++;
			}
			
			i++;
		}
		normalizeHierarchicalCoverage();
		
	}

}
