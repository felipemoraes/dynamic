package br.ufmg.dcc.latin.user;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class RelevanceSet {
	
	Map<String,List<Passage>> data;

	public RelevanceSet(){
		data = new HashMap<String, List<Passage>>();
	}
	
	public void add(String topicId, Passage passage){
		if (!data.containsKey(topicId)){
			data.put(topicId, new ArrayList<Passage>());
		}
		data.get(topicId).add(passage);
	}
	
	public Passage[] get(String topicId){
		
		if (!data.containsKey(topicId)) {
			return null;
		}
		int n = data.get(topicId).size();
		Passage[] passages = new Passage[n];
		for (int i = 0; i < passages.length; i++) {
			passages[i] = data.get(topicId).get(i);
		}
		return passages;
	}
	
	public double getRelevance(String topicId, String aspectId){
		
		double rel = 0;
		if (!data.containsKey(topicId)) {
			return 0;
		}

		for (int i = 0; i < data.get(topicId).size(); i++) {
			Passage passage = data.get(topicId).get(i);
			if (passage.aspectId.equals(aspectId)) {
				rel = Math.max(passage.relevance,rel);
			}
		}
		return rel;
	}
	
}