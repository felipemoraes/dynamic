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

public class FlatAspectGTController implements AspectController {
	
	public Feedback[] feedbacks;
	
	public int n;
	
	public Map<String,String> subtopicsNames;
	
	public float[] importance;
	public float[] novelty;
	public float[][] coverage;
	
	public float[] accumulatedRelevance;
	
	public float[][][] features;
	
	public float[] v;
	public float[] s;
	
	private FlatAspectModel flatAspectModel;
	
	public FlatAspectGTController(){
		
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
		accumulatedRelevance = new float[aspectSize];
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		int i = 0;
		
		for (String aspectId : flatAspectModel.getAspects()) {


		
			String aspectComponent = RetrievalCache.query + " " + subtopicsNames.get(aspectId);
			System.out.println(aspectComponent);
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
		printCoverage();

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
		System.out.println("------------------------");
		for (int i = 0; i < 100; i++) {
			for (int j = 0; j < coverage[i].length; j++) {
				System.out.print(coverage[i][j] + " ");
			}
			System.out.println();
		}
		System.out.println("------------------------");
	}
	
	private float[] scaling(float[] scores){
		float min = Float.POSITIVE_INFINITY;
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] < min) {
				min = scores[i];
			}
		}
		
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > max) {
				max = scores[i];
			}
		}
		
		for (int i = 0; i < scores.length; i++) {
			if (max!=min) {
				scores[i] = (scores[i]-min)/(max-min);
			} else {
				scores[i] = 0;
			}
			
		}
		return scores;
		
	}
	
	public void cacheFeedback(Feedback[] feedbacks){
		
		if (this.feedbacks == null) {
			this.feedbacks = new Feedback[n];
		}
		
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < feedbacks.length; j++) {
				if (feedbacks[j].getDocno().equals(RetrievalCache.docnos[i])){
					this.feedbacks[i] = feedbacks[j]; 
				}
			}
		}
	}

	@Override
	public void miningProportionalAspects(Feedback[] feedbacks) {
		// TODO Auto-generated method stub

	}

}
