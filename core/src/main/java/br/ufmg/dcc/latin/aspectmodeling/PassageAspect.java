package br.ufmg.dcc.latin.aspectmodeling;

import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

public class PassageAspect {
	private TIntObjectMap<TDoubleArrayList> aspectTree;
	
	public PassageAspect(){
		aspectTree = new TIntObjectHashMap<TDoubleArrayList>();
	}
	
	public void addPassage(int passageId, double relevance){
		if (!aspectTree.containsKey(passageId)) {
			aspectTree.put(passageId, new TDoubleArrayList());
		} 
		aspectTree.get(passageId).add(relevance);
	}
	
	public int[] getPassages(){
		return aspectTree.keys();
	}
	
	public double getPassageRelevance(int passageId){
		
		TDoubleArrayList relevances = aspectTree.get(passageId);
		double relevance = 0;
		for (int i = 0; i < relevances.size(); i++) {
			relevance += relevances.get(i);
		}
		
		relevance /= relevances.size();
		return relevance;

	}
	
}
