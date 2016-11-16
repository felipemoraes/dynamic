package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.querying.ResultSet;

public class TrecUser implements User {
	
	private static Map<String,RelevanceSet> repository;
	
	public static void load(String topicFilename){
		
		repository = new HashMap<String,RelevanceSet>();
		try (BufferedReader br = new BufferedReader(new FileReader(topicFilename))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(",",5);
		    	Passage passage = new Passage(splitLine[2],splitLine[4],Integer.parseInt(splitLine[3]));
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
	
	public static Feedback get(String docId, String topicId){
		Feedback feedback = new Feedback();
		feedback.setTopicId(topicId);
		feedback.setDocno(docId);
		if (!repository.containsKey(docId)){
			feedback.setOnTopic(false);
			
			return feedback;
		} 
		Passage[] passages = repository.get(docId).get(topicId);
		if (passages == null) {
			feedback.setOnTopic(false);
			return feedback;
		} else {
			
			feedback.setPassages(passages);
			feedback.setOnTopic(true);
		}
		return feedback;
	}

	public static Feedback[] get(ResultSet resultSet, String topicId) {
		int n = resultSet.docids.length;
		Feedback[] feedbacks = new Feedback[n];
		for (int i = 0; i < resultSet.docids.length; i++) {
			feedbacks[i] = TrecUser.get(resultSet.docnos[i], topicId);
		}
		return feedbacks;
	}
	
}