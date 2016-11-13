package br.ufmg.dcc.latin.controller;

import java.util.Arrays;

import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.Similarity;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.diversity.scoring.Scorer;
import br.ufmg.dcc.latin.diversity.scoring.xQuAD;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.querying.SelectedSet;


public class FlatAspectController implements AspectController {
	
	
	private FlatAspectModel flatAspectModel;

	public FlatAspectController(){
		
		AspectCache.n = RetrievalCache.docids.length;
		AspectCache.importance = new float[0];
		AspectCache.novelty = new float[0];
		AspectCache.coverage = new float[AspectCache.n][0];
		AspectCache.v = new float[0];
		AspectCache.s = new float[0];
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
			return;
		}
		AspectCache.importance = new float[aspectSize];
		AspectCache.novelty = new float[aspectSize];
		AspectCache.coverage = new float[n][aspectSize];
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(AspectCache.importance, uniformImportance);
		Arrays.fill(AspectCache.novelty, 1.0f);
		int i = 0;
		Similarity similarity = new BM25Similarity();
		for (String aspectId : flatAspectModel.getAspects()) {

			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
			    float[] scores = RetrievalController.getSimilarities(RetrievalCache.docids, aspectComponent,similarity);
			    for(int j = 0;j< n ;++j) {

			    	float score = scores[j];
			    	if (AspectCache.coverage[j][i] < score) {
			    		AspectCache.coverage[j][i] = score;
			    	}
			    }
			}
	
			for(int j = 0;j< n ;++j) {
				if (AspectCache.feedbacks[j] != null) {
					float score = AspectCache.feedbacks[j].getRelevanceAspect(aspectId);
					AspectCache.coverage[j][i] = score;
				}
			}
			i++;
		}
		normalizeCoverage();
	}
	
	public float[] similaritiesFromAspects(int maxRank) {
		float[] sims = new float[AspectCache.n];
		Arrays.fill(sims, 0);
		
		if (AspectCache.coverage == null){
			return sims;
		}
		
		for (int i = 0; i < sims.length; i++) {
			
			sims[i] = cosine(AspectCache.coverage[maxRank],AspectCache.coverage[i]);
		}
		
		return sims;
	}
	
	private float cosine(float[] v1, float[] v2){
		float denom = 0;
		float sum1 = 0;
		float sum2  = 0;
	
		for (int i = 0; i < v2.length; i++) {
			denom += v1[i]*v2[i];
		}
		
		for (int i = 0; i < v2.length; i++) {
			sum1 += v1[i]*v1[i];
			sum2 += v2[i]*v2[i];
		}
		sum1 = (float) Math.sqrt(sum1);
		sum2 = (float) Math.sqrt(sum2);
		
		if (sum1*sum2 > 0){
			return denom/(sum1*sum2);
		} 
		
		return 0;
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
			AspectCache.v = new float[0];
			AspectCache.s = new float[0];
			AspectCache.coverage = new float[n][0];
			return;
		}
		
		AspectCache.v = new float[aspectSize];
		AspectCache.s = new float[aspectSize];
		AspectCache.coverage = new float[n][aspectSize];
		Arrays.fill(AspectCache.v, 1.0f);
		Arrays.fill(AspectCache.s, 1.0f);

		int i = 0;
		Similarity similarity = new BM25Similarity();
		for (String aspectId : flatAspectModel.getAspects()) {

			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
			    float[] scores = RetrievalController.getSimilarities(RetrievalCache.docids, aspectComponent,similarity);
			    for(int j = 0;j< n ;++j) {

			    	float score = scores[j];
			    	if (AspectCache.coverage[j][i] < score) {
			    		AspectCache.coverage[j][i] = score;
			    	}
			    }
			}
	
			for(int j = 0;j< n ;++j) {
				if (AspectCache.feedbacks[j] != null) {
					float score = AspectCache.feedbacks[j].getRelevanceAspect(aspectId);
					AspectCache.coverage[j][i] = score;
				}
			}
			i++;
		}
		normalizeCoverage();
		
	}
	
	
	public void cacheFeedback(Feedback[] feedbacks){
		
		
		if (AspectCache.feedbacks == null) {
			int n = RetrievalCache.docnos.length;
			AspectCache.feedbacks = new Feedback[n];
		}
		
		if (RetrievalCache.docnos == null) {
			return;
		}

		for (int i = 0; i < RetrievalCache.docnos.length; i++) {
			for (int j = 0; j < feedbacks.length; j++) {
				if (feedbacks[j].getDocno() == RetrievalCache.docnos[i]){
					AspectCache.feedbacks[i] = feedbacks[j]; 
				}
			}
		}
	}
	
	
	public void normalizeCoverage(){
		for (int i = 0; i < AspectCache.coverage[0].length; ++i) {
			float sum = 0;
			for (int j = 0; j < AspectCache.coverage.length; j++) {
				sum +=  AspectCache.coverage[j][i];
			}
			
			for (int j = 0; j < AspectCache.coverage.length; j++) {
				if (sum > 0) {
					float normValue = AspectCache.coverage[j][i]/sum;
					AspectCache.coverage[j][i] = normValue;
				}
				
			}
		}
	}
	
	public void updateNovelty(SelectedSet selected){
		
		for (int j = 0; j < RetrievalCache.docids.length; ++j) {
			if (! selected.has(RetrievalCache.docids[j])) {
				continue;
			}
			for (int i = 0; i < AspectCache.novelty.length; i++) {
				float newNovelty = AspectCache.novelty[i]*(1-AspectCache.coverage[j][i]);
				AspectCache.novelty[i] = newNovelty;
			}
		}
		normalizeNovelty();
	}
	
	public void normalizeNovelty(){
		float sum = 0;
		for (int i = 0; i < AspectCache.novelty.length; i++) {
			sum += AspectCache.novelty[i];
		}
		for (int i = 0; i < AspectCache.novelty.length; i++) {
			if (sum > 0) {
				float normValue = AspectCache.novelty[i]/sum;
				AspectCache.novelty[i] = normValue;
			}
		}
	}
	
	public void clear(){
		AspectCache.importance = null;
		AspectCache.novelty = null;
		AspectCache.coverage = null;
		AspectCache.feedbacks = null;
		AspectCache.s = null;
		AspectCache.v = null;
		flatAspectModel = null;
	}


	public void printCoverage() {
		for (int i = 0; i < AspectCache.coverage.length; i++) {
			for (int j = 0; j < AspectCache.coverage[i].length; j++) {
				System.out.print(AspectCache.coverage[i][j] + " ");
			}
		}
		
	}
	
	public void printNovelty() {
		for (int i = 0; i < AspectCache.novelty.length; i++) {
			System.out.print(AspectCache.novelty[i] + " ");
		}
		System.out.println();
	}


	public void mining(Feedback[] feedback, Scorer scorer) {
		if (scorer instanceof xQuAD){
			miningDiversityAspects(feedback);
		}
		
	}

}
