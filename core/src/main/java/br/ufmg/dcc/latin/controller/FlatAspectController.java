package br.ufmg.dcc.latin.controller;

import java.util.Arrays;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.querying.SelectedSet;


public class FlatAspectController implements AspectController {
	
	public Feedback[] feedbacks;

	public int n;
	
	public float[] importance;
	public float[] novelty;
	public float[][] coverage;
	
	public float[][][] features;
	
	public float[] v;
	public float[] s;
	
	private SelectedSet selected;
	
	private FlatAspectModel flatAspectModel;

	public FlatAspectController(){
		
		n = RetrievalCache.docids.length;
		importance = new float[0];
		novelty = new float[0];
		coverage = new float[n][0];
		v = new float[0];
		s = new float[0];
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

		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			return;
		}
		importance = new float[aspectSize];
		novelty = new float[aspectSize];
		coverage = new float[n][aspectSize];
		
		features = new float[n][aspectSize][];
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 1.0f);
		int i = 0;
		
		for (String aspectId : flatAspectModel.getAspects()) {

			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
			    float[] scores = RetrievalController.getSimilarities(RetrievalCache.docids, aspectComponent);
			    for(int j = 0;j< n ;++j) {

			    	float score = scores[j];
			    	if (coverage[j][i] < score) {
			    		coverage[j][i] = score;
			    	}
			    }
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
	
	public float[] similaritiesFromAspects(int maxRank) {
		float[] sims = new float[n];
		Arrays.fill(sims, 0);
		
		if (coverage == null){
			return sims;
		}
		
		for (int i = 0; i < sims.length; i++) {
			
			sims[i] = cosine(coverage[maxRank],coverage[i]);
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
		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			v = new float[0];
			s = new float[0];
			coverage = new float[n][0];
			return;
		}
		
		v = new float[aspectSize];
		s = new float[aspectSize];
		coverage = new float[n][aspectSize];
		Arrays.fill(v, 1.0f);
		Arrays.fill(s, 1.0f);

		int i = 0;
		
		for (String aspectId : flatAspectModel.getAspects()) {

			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
			    float[] scores = RetrievalController.getSimilarities(RetrievalCache.docids, aspectComponent);
			    scores = normalize(scores);
			    for(int j = 0;j< n ;++j) {

			    	float score = scores[j];
			    	if (coverage[j][i] < score) {
			    		coverage[j][i] = score;
			    	}
			    }
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
	

	
	
	public void cacheFeedback(Feedback[] feedbacks){
		
		if (this.feedbacks == null) {
			this.feedbacks = new Feedback[n];
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < feedbacks.length; j++) {
				if (feedbacks[j].getDocno() == RetrievalCache.docnos[i]){
					this.feedbacks[i] = feedbacks[j]; 
				}
			}
		}
	}
	
	private float[] normalize(float[] values){
		float sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		for (int i = 0; i < values.length; i++) {
			if (sum > 0) {
				values[i] = values[i]/sum;
			}
			
		}
		return values;
	}
	
	
	public void normalizeCoverage(){
		for (int i = 0; i < coverage[0].length; ++i) {
			float sum = 0;
			for (int j = 0; j < coverage.length; j++) {
				sum += coverage[j][i];
			}
			
			for (int j = 0; j < coverage.length; j++) {
				if (sum > 0) {
					float normValue = coverage[j][i]/sum;
					coverage[j][i] = normValue;
				}
				
			}
		}
	}

	public void printCoverage() {
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[i].length; j++) {
				System.out.print(coverage[i][j] + " ");
			}
		}
		
	}
	
	public void printNovelty() {
		for (int i = 0; i < novelty.length; i++) {
			System.out.print(novelty[i] + " ");
		}
		System.out.println();
	}


	public SelectedSet getSelected() {
		return selected;
	}


	public void setSelected(SelectedSet selected) {
		this.selected = selected;
	}


	public void miningFeatures() {

		int i = 0;
		
		for (String aspectId : flatAspectModel.getAspects()) {

			for (String aspectComponent: flatAspectModel.getAspectComponents(aspectId)) {
				
			    float[] scores = RetrievalController.getSimilarities(RetrievalCache.docids, aspectComponent);
			    
			    for(int j = 0;j< n ;++j) {

			    	float score = scores[j];
			    	if (coverage[j][i] < score) {
			    		coverage[j][i] = score;
			    	}
			    }
			}
	
			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					float score = this.feedbacks[j].getRelevanceAspect(aspectId);
					coverage[j][i] = score;
				}
			}
			i++;
		}
		
	}

}
