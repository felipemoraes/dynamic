package br.ufmg.dcc.latin.diversity;

import java.util.ArrayList;
import java.util.HashMap;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlatAspectModel {
	// must be instantiated with LinkedHashMap to preserve order
	private Map<String,HashMap<String,List<Integer>>> modelContainer;
	
	public FlatAspectModel(){
		modelContainer = new LinkedHashMap<String,HashMap<String,List<Integer>>>();
	}
	
	public void addToAspect(String aspectId, String aspectComponent, Integer rel){
		if (! modelContainer.containsKey(aspectId)) {
			modelContainer.put(aspectId, new HashMap<String,List<Integer>>());
		}
		if (!modelContainer.get(aspectId).containsKey(aspectComponent)){
			modelContainer.get(aspectId).put(aspectComponent, new ArrayList<Integer>());
		}
		modelContainer.get(aspectId).get(aspectComponent).add(rel);
	}
	
	
	public Set<String> getAspectComponents(String aspectId){
		return modelContainer.get(aspectId).keySet();
	}
	
	public Map<String,List<Integer>> getAspectComponentsAndWeights(String aspectId){
		return modelContainer.get(aspectId);
	}
	
	public Set<String> getAspects(){
		return modelContainer.keySet();
	}
	
	public float getAspectWeight(String aspectId, String aspectComponent){
		float sum = 0;
		
		List<Integer> rels = modelContainer.get(aspectId).get(aspectComponent);
		
		for (Integer value : rels) {
			sum += value;
		}
		if (sum > 0) {
			return sum/rels.size()/4;
		}
		
		return 0;
	}
	
	
}
