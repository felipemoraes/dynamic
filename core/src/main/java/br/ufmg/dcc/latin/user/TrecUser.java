package br.ufmg.dcc.latin.user;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.LaplaceDistribution;
import org.apache.commons.math3.stat.StatUtils;


import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.simulation.SimAP;

public class TrecUser implements User {
	
    private Map<String,RelevanceSet> repository;
    
	public String topicId;
	
	private static TrecUser trecUser;
	
	
	public static TrecUser getInstance(String topicFilename){
		
		if (trecUser == null) {
			trecUser = new TrecUser(topicFilename);
		}
		
		return trecUser;

	}
	
	public static Map<String,double[]> subtopicsCoverage;
	public static Map<String,double[]> subtopicsCoverageSorted;
	public static Map<String,int[]> subtopicsCoverageIndices;
	
	public static int originalSize;
	public static double allKlDiv;
	
	private TrecUser(String topicFilename){
		
		repository = new HashMap<String,RelevanceSet>();
		try (BufferedReader br = new BufferedReader(new FileReader(topicFilename))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(",",5);
		    	Passage passage = new Passage(splitLine[2],Integer.parseInt(splitLine[4]),Integer.parseInt(splitLine[3]));
		    	if (passage.relevance == 0 ) {
		    		passage.relevance = 1;
		    	}
		    	if (!repository.containsKey(splitLine[0])){
		    		repository.put(splitLine[0], new RelevanceSet());
		    	} 
		    	
		    	repository.get(splitLine[0]).add(splitLine[1], passage);
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public Feedback get(String docId){
		Feedback feedback = new Feedback();
		feedback.topicId = topicId;
		feedback.docno = docId;
		if (!repository.containsKey(docId)){
			feedback.onTopic = false;
			
			return feedback;
		} 
		Passage[] passages = repository.get(docId).get(topicId);
		if (passages == null) {
			feedback.onTopic = false;
			return feedback;
		} else {
			
			feedback.passages = passages;
			feedback.onTopic = true;
		}
		return feedback;
	}
	
	public void generateSubtopics(double targetAP, String[] docnos){
		
		if (subtopicsCoverageSorted == null) {
			
			subtopicsCoverage = new HashMap<String, double[]>();
			subtopicsCoverageSorted  = new HashMap<String, double[]>();
			subtopicsCoverageIndices = new HashMap<String, int[]>();
			
			int n = docnos.length;
			for (int i = 0; i < docnos.length; i++) {
				if (!repository.containsKey(docnos[i])){
					continue;
				} 
				
				Passage[] passages = repository.get(docnos[i]).get(topicId);
				if (passages == null) {
					continue;
				}
				for (int j = 0; j < passages.length; j++) {
					if (!subtopicsCoverage.containsKey(passages[j].subtopicId)){
						subtopicsCoverage.put(passages[j].subtopicId, new double[n]);
					}
				}
			}
			
			for (String subtopicId : subtopicsCoverage.keySet()) {
				double[] relevances = getRelevances(subtopicId, docnos);
				int[] sortedIndices = IntStream.range(0, relevances.length)
		                .boxed().sorted((i, j) -> ((new Double(relevances[i])).compareTo(new Double(relevances[j])) ) )
		                .mapToInt(ele -> ele).toArray();
				
				for (int i = 0; i < sortedIndices.length/2; i++) {
				     int temp = sortedIndices[i];
				     sortedIndices[i] = sortedIndices[sortedIndices.length-1 - i];
				     sortedIndices[sortedIndices.length-1 - i] = temp;
				 }
				
			
				double[] scores = new double[relevances.length];
				for (int i = 0; i < sortedIndices.length; i++) {
					scores[i] = relevances[sortedIndices[i]];
					
				}
				
				subtopicsCoverageIndices.put(subtopicId, sortedIndices);
				subtopicsCoverageSorted.put(subtopicId, scores);
			}
		}
		
		double allAPs = 0;
		SimAP.targetAP = targetAP;
		for (String subtopicId : subtopicsCoverage.keySet()) {
			
			double[] relevances = getRelevances(subtopicId, docnos);
			

			int[] ids = subtopicsCoverageIndices.get(subtopicId);
			double[] scores = new double[relevances.length];
			
			double[] localScores =  SimAP.apply(ids, subtopicsCoverageSorted.get(subtopicId));
			
		
			for (int i = 0; i < scores.length; i++) {
				scores[ids[i]] = localScores[i];
			}
			allAPs += SimAP.currentAP;
			subtopicsCoverage.put(subtopicId, scores);
		}
		if (subtopicsCoverage.size() == 0 || allAPs == 0 )  {
			SimAP.currentAP = 0;
		} else if (subtopicsCoverage.size() != 0 ){
			SimAP.currentAP = allAPs/subtopicsCoverage.size();
		}
		
	
	}
	
	public void destroySubtopics(){
		subtopicsCoverageSorted = null;
		subtopicsCoverage = null;
	}
	
	public void generateSubtopics(String[] docnos){
		subtopicsCoverage = new HashMap<String, double[]>();
		int n = docnos.length;
		for (int i = 0; i < docnos.length; i++) {
			if (!repository.containsKey(docnos[i])){
				continue;
			} 
			
			Passage[] passages = repository.get(docnos[i]).get(topicId);
			if (passages == null) {
				continue;
			}
			for (int j = 0; j < passages.length; j++) {
				if (!subtopicsCoverage.containsKey(passages[j].subtopicId)){
					subtopicsCoverage.put(passages[j].subtopicId, new double[n]);
				}
			}
		}
		
		for (String subtopicId : subtopicsCoverage.keySet()) {
			double[] relevances = getRelevances(subtopicId, docnos);
			subtopicsCoverage.put(subtopicId, relevances);
		}
	}
	
	public double get(String subtopicId, String docId){
		
		if (!repository.containsKey(docId)){
			return 0;
		} 
		Passage[] passages = repository.get(docId).get(topicId);
		if (passages == null) {
			return 0;
		} else {
			double score = 0;
			for (int i = 0; i < passages.length; i++) {
				if (passages[i].subtopicId.equals(subtopicId)){
					score = Math.max(passages[i].relevance, score);
				}
			}
			return score;
		}
	}
	
	public double getScore(String docId){
		if (!repository.containsKey(docId)){
			return 0;
		} 
		Passage[] passages = repository.get(docId).get(topicId);
		if (passages == null) {
			return 0;
		} else {
			double score = 0;
			for (int i = 0; i < passages.length; i++) {
				score = Math.max(passages[i].relevance, score);
			}
			return score;
		}
	}
	
	public double[] getRelevances(String subtopicId, String[] docnos){
		int n = docnos.length;
		double[] relevances = new double[n];
		for (int i = 0; i < n; i++) {
			relevances[i] = get(subtopicId,docnos[i]);
		}
		
		return relevances;
	}
	
	public double[] get(String subtopicId, String[] docnos){
		return subtopicsCoverage.get(subtopicId);
	}
	
	public double[] get(String[] docnos){
		int n = docnos.length;
		double[] relevances = new double[n];
		for (int i = 0; i < n; i++) {
			relevances[i] = getScore(docnos[i]);
		}
		return relevances;
	}

	public Feedback[] get(ResultSet resultSet) {
		int n = resultSet.docids.length;
		Feedback[] feedbacks = new Feedback[n];
		
		if (resultSet.docnos == null) {
			return feedbacks;
		}
		
		for (int i = 0; i < resultSet.docids.length; i++) {
			feedbacks[i] = trecUser.get(resultSet.docnos[i]);
			feedbacks[i].index = resultSet.index[i];
		}
		return feedbacks;
	}


	
	public void generateSubtopicsWithNoiseDroped(String[] docnos, double frac) {
		if (subtopicsCoverage == null) {
			Random rand = new Random();
			subtopicsCoverage = new HashMap<String, double[]>();
			int n = docnos.length;
			for (int i = 0; i < docnos.length; i++) {
				if (!repository.containsKey(docnos[i])){
					continue;
				} 
				
				Passage[] passages = repository.get(docnos[i]).get(topicId);
				if (passages == null) {
					continue;
				}
				for (int j = 0; j < passages.length; j++) {
					if (!subtopicsCoverage.containsKey(passages[j].subtopicId)){
						subtopicsCoverage.put(passages[j].subtopicId, new double[n]);
					}
				}
			}
			
			int subTopicsSize = subtopicsCoverage.size();
			originalSize = subTopicsSize;
			String[] subTopics = new String[subTopicsSize];
			int k = 0;
			for (String subtopic : subtopicsCoverage.keySet()) {
				subTopics[k] = subtopic;
				k++;
			}
			Set<String> dropAspect  = new HashSet<String>();
			int drops = (int) Math.ceil(((1-frac)*subTopicsSize));
			
			while (dropAspect.size() < drops) {
				int next = rand.nextInt(subTopicsSize);
				if (!dropAspect.contains(subTopics[next])) {
					dropAspect.add(subTopics[next]);
				}
			}
			for (String subtopic : dropAspect) {
				subtopicsCoverage.remove(subtopic);
			}
		
			for (String subtopicId : subtopicsCoverage.keySet()) {
				double[] relevances = getRelevances(subtopicId, docnos);
				subtopicsCoverage.put(subtopicId, relevances);
			}
			
		} else {
			for (String subtopicId : subtopicsCoverage.keySet()) {
				double[] relevances = getRelevances(subtopicId, docnos);
				subtopicsCoverage.put(subtopicId, relevances);
			}
		}
		
	}
		
	public double generateSubtopicsWithNoise(double noise, String[] docnos) {
		subtopicsCoverage = new HashMap<String, double[]>();
		int n = docnos.length;
		for (int i = 0; i < docnos.length; i++) {
			if (!repository.containsKey(docnos[i])){
				continue;
			} 
			
			Passage[] passages = repository.get(docnos[i]).get(topicId);
			if (passages == null) {
				continue;
			}
			for (int j = 0; j < passages.length; j++) {
				if (!subtopicsCoverage.containsKey(passages[j].subtopicId)){
					subtopicsCoverage.put(passages[j].subtopicId, new double[n]);
				}
			}
		}
		double allKlDiv = 0;
		for (String subtopicId : subtopicsCoverage.keySet()) {
			
			double[] relevances = getRelevances(subtopicId, docnos);
			StatUtils.normalize(relevances);
			double sum = StatUtils.sum(relevances);
			double[] probs = new double[relevances.length];
			for (int i = 0; i < probs.length; i++) {
				probs[i] = relevances[i]/sum;
			}
			
			double min = StatUtils.min(relevances);
			double max = StatUtils.max(relevances);
			double deltaF = max - min;
			double epsilon = noise;
			LaplaceDistribution dist = new LaplaceDistribution(0,deltaF/epsilon);
			
			for (int i = 0; i < relevances.length; i++) {
				relevances[i] += dist.sample();
			}
			
			min = StatUtils.min(relevances);
			max = StatUtils.max(relevances);
			for (int i = 0; i < probs.length; i++) {
				relevances[i] = (relevances[i]-min)/(max-min);
				relevances[i] *= 4;
			}
			
			sum = StatUtils.sum(relevances);
			double[] probsNoised = new double[relevances.length];
			for (int i = 0; i < probs.length; i++) {
				probsNoised[i] = relevances[i]/sum;
			}
			allKlDiv += klDivergence(probs,probsNoised);
			subtopicsCoverage.put(subtopicId, relevances);
		}
		if (subtopicsCoverage.size() > 0) {
			allKlDiv /= subtopicsCoverage.size();
		} else {
			allKlDiv = 0;
		}
		return allKlDiv;
	}
	
    public static double klDivergence(double[] p1, double[] p2) {


        double klDiv = 0.0;

        for (int i = 0; i < p1.length; ++i) {
          if (p1[i] == 0) { continue; }
          if (p2[i] == 0.0) { continue; } // Limin

          klDiv += p1[i] * Math.log( p1[i] / p2[i] );
        }

        return klDiv / log2; // moved this division out of the loop -DM
      }

    public static final double log2 = Math.log(2);


	public void destroySubtopicsDroped(double drop) {
		int oldDrop = originalSize - subtopicsCoverage.size();
		int drops = (int) Math.ceil(((1-drop)*originalSize));
		if (drops != oldDrop) {
			destroySubtopics();
		}
	}
	
}
