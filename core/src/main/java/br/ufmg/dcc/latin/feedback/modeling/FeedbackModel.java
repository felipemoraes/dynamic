package br.ufmg.dcc.latin.feedback.modeling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

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
		if (!subtopicsId.containsKey(subtopicId)) {
			return -1;
		}
		return subtopicsId.get(subtopicId);
	}
	
	public FeedbackModel drop(double frac){
		Random rand = new Random();
		FeedbackModel model = new FeedbackModel();
		int drops = (int) Math.ceil(((1-frac)*subtopicsId.size()));
		Set<Integer> dropSet = new HashSet<Integer>();
		
		while (dropSet.size() < drops) {
			int next = rand.nextInt(subtopicsIndices.size());
			if (!dropSet.contains(dropSet)) {
				dropSet.add(next);
			}
		}
		for (Integer selected : dropSet) {
			model.addSubtopic(subtopicsIndices.get(selected));
		} 
		return model;
	}
	
}
