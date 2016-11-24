package br.ufmg.dcc.latin.controller;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;

public class SubtopicNameAspectMining extends AspectMining {

	
	private Map<String,String> subtopicsNames;
	
	
	private FlatAspectModel flatAspectModel;
	
	public SubtopicNameAspectMining(){
		
		n = RetrievalCache.docids.length;
		importance = new float[0];
		novelty = new float[0];
		coverage = new float[n][0];
		v = new float[0];
		s = new float[0];
		accumulatedRelevance = new float[0];
		
		readAspectsName();
	}
	
	
	private void readAspectsName() {
		subtopicsNames = new HashMap<String,String>();
		try (BufferedReader br = new BufferedReader(new FileReader("../share/subtopics_names.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split("\t",3);
		    	subtopicsNames.put(splitLine[1],splitLine[2]);
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}


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
				flatAspectModel.addToAspect(passages[j].getAspectId(), passages[j].getText(),passages[j].getRelevance());
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
		v = new float[aspectSize];
		s = new float[aspectSize];
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		Arrays.fill(v, 1.0f);
		Arrays.fill(s, 1.0f);
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {


		
			String aspectComponent = RetrievalCache.query + " " + subtopicsNames.get(aspectId);
			
			float[] scores = null;
			if (RetrievalCache.subtopicsCache.containsKey(aspectComponent)) {
				scores = RetrievalCache.subtopicsCache.get(aspectComponent);
			} else {
				scores = RetrievalController.getSimilaritiesRerank(RetrievalCache.docids, aspectComponent);
				RetrievalCache.subtopicsCache.put(aspectComponent, scores);
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
	
			String aspectComponent = RetrievalCache.query + " " + subtopicsNames.get(aspectId);
			
			float[] scores = null;
			if (RetrievalCache.subtopicsCache.containsKey(aspectComponent)) {
				scores = RetrievalCache.subtopicsCache.get(aspectComponent);
			} else {
				scores = RetrievalController.getSimilaritiesRerank(RetrievalCache.docids, aspectComponent);
				RetrievalCache.subtopicsCache.put(aspectComponent, scores);
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
