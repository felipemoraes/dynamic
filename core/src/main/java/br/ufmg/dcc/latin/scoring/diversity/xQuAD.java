package br.ufmg.dcc.latin.scoring.diversity;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.diversity.FlatAspect;

public class xQuAD {
	
	 List<FlatAspect> importance;
	 List<FlatAspect[]> coverage;
	 List<FlatAspect> novelty;
	 
	public xQuAD(List<FlatAspect> importance, List<FlatAspect[]> coverage,List<FlatAspect> novelty){
		this.importance = importance;
		this.coverage = coverage;
		this.novelty = novelty;
	}
	
	public float div(int d ){
		float diversity = 0;
		for (int i = 0; i < importance.size(); i++) {
			diversity += importance.get(i).getValue()*coverage.get(i)[d].getValue()*novelty.get(i).getValue();
		}
		return diversity;
	}
	
	public void update(int d){
		for (int i = 0; i < novelty.size(); i++) {
			float nov = novelty.get(i).getValue();
			novelty.get(i).setValue(nov*(1-coverage.get(i)[d].getValue()));
		}
		
	}
	
	public void update(List<FlatAspect> importance, List<FlatAspect[]> coverage,List<FlatAspect> novelty){
		this.importance = importance;
		this.coverage = coverage;
		this.novelty = novelty;
	}

}
