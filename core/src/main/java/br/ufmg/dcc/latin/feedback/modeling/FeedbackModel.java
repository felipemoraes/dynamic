package br.ufmg.dcc.latin.feedback.modeling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FeedbackModel {
	Map<String,Integer> subtopicsId;
	List<String> subtopicsIndices;
	
	public int size;
	
	public FeedbackModel() {
		subtopicsId = new HashMap<String,Integer>();
		subtopicsIndices = new ArrayList<String>();
		size = 0;
	}
	
	public void addSubtopic(String subtopicId){
		if (!subtopicsId.containsKey(subtopicId)){
			subtopicsId.put(subtopicId,size);
			subtopicsIndices.add(subtopicId);
			size++;
		}
	}
	
	public String getSubtopicId(int subtopicId){
		return subtopicsIndices.get(subtopicId);
	}
	
	public int getSubtopicId(String subtopicId){
		return subtopicsId.get(subtopicId);
	}
	
}
