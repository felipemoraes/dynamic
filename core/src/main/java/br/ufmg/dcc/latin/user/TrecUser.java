package br.ufmg.dcc.latin.user;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

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
		SimAP.targetAP = targetAP;
		double allAPs = 0;
		for (String subtopicId : subtopicsCoverage.keySet()) {
			double[] relevances = getRelevances(subtopicId, docnos);
			int[] sortedIndices = IntStream.range(0, relevances.length)
	                .boxed().sorted((i, j) -> ((new Double(relevances[i])).compareTo(new Double(relevances[j])) ) )
	                .mapToInt(ele -> ele).toArray();
			for(int i = 0; i < sortedIndices.length / 2; i++)
			{
			    int temp = sortedIndices[i];
			    sortedIndices[i] = sortedIndices[sortedIndices.length - i - 1];
			    sortedIndices[sortedIndices.length - i - 1] = temp;
			}
			for (int i = 0; i < sortedIndices.length; i++) {
				relevances[i] = relevances[sortedIndices[i]];
			}
			double[] scores =  SimAP.apply(relevances);
			
			for (int i = 0; i < scores.length; i++) {
				scores[sortedIndices[i]] = scores[i];
			}
			allAPs = Math.max(allAPs,SimAP.currentAP);
			subtopicsCoverage.put(subtopicId, scores);
		}
		SimAP.currentAP = allAPs;
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
		for (int i = 0; i < resultSet.docids.length; i++) {
			feedbacks[i] = trecUser.get(resultSet.docnos[i]);
		}
		return feedbacks;
	}

	
}
