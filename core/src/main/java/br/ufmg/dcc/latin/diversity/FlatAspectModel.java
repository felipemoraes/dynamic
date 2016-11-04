package br.ufmg.dcc.latin.diversity;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class FlatAspectModel {
	// must be instantiated with LinkedHashMap to preserve order
	private Map<String,HashSet<String>> modelContainer;
	
	public FlatAspectModel(){
		modelContainer = new LinkedHashMap<String,HashSet<String>>();
	}
	
	public void addToAspect(String aspectId, String aspectComponent){
		if (! modelContainer.containsKey(aspectId)) {
			modelContainer.put(aspectId, new HashSet<>());
		}
		modelContainer.get(aspectId).add(aspectComponent);
	}
	
	
	public Set<String> getAspectComponents(String aspectId){
		return modelContainer.get(aspectId);
	}
	
	
	public Set<String> getAspects(){
		return modelContainer.keySet();
	}
	
	
	
}
