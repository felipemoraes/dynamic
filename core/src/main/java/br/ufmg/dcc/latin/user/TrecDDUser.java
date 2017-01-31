package br.ufmg.dcc.latin.user;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.latin.baselineranker.ResultList;

public class TrecDDUser extends User {
	
	private Map<String, RelevanceSet> repository;
	

	
	public TrecDDUser(){
		
		repository = new HashMap<String,RelevanceSet>();
		try (BufferedReader br = new BufferedReader(new FileReader("../share/truth_data.txt"))) {
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
	
	public FeedbackList get(String docno, int docid, String topicId){
		FeedbackList feedbackList = new FeedbackList();
		
		
		if (!repository.containsKey(docno)){
			return feedbackList;
		}
		
		
		Passage[] passages = repository.get(docno).get(topicId);
		if (passages == null) {
			return feedbackList;
		} else {
			for (int i = 0; i < passages.length; i++) {
				Feedback feedback = new Feedback();
				feedback.aspectId = passages[i].aspectId;
				feedback.docid = docid;
				feedback.docno = docno;
				feedback.relevance = passages[i].relevance;
				feedback.passageId = passages[i].passageId;
				feedbackList.feedbacks.add(feedback);
			}
		}
		return feedbackList;
	}

	@Override
	public FeedbackList getFeedbackSet(String topicId, ResultList resultList) {
		FeedbackList feedbackList = new FeedbackList();
		
		for (int i = 0; i < resultList.docids.length; i++) {
			
			feedbackList.feedbacks.addAll(get(resultList.docnos[i],resultList.docids[i],topicId).feedbacks);
		}

		return feedbackList;
	}

}
