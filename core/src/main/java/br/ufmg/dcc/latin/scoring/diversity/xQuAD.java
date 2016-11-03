package br.ufmg.dcc.latin.scoring.diversity;

import java.util.List;

import br.ufmg.dcc.latin.diversity.FlatAspect;
import br.ufmg.dcc.latin.scoring.DiversityScorer;


public class xQuAD extends DiversityScorer{
	
	 List<FlatAspect> importance;
	 List<FlatAspect[]> coverage;
	 List<FlatAspect> novelty;
	 
	public xQuAD(List<FlatAspect> importance, List<FlatAspect[]> coverage,List<FlatAspect> novelty){
		this.importance = importance;
		this.coverage = coverage;
		this.novelty = novelty;
	}
	
	@Override
	public float div(int d){
		float diversity = 0;
		for (int i = 0; i < importance.size(); i++) {
			diversity += importance.get(i).getValue()*coverage.get(i)[d].getValue()*novelty.get(i).getValue();
		}
		return diversity;
	}
	
	@Override
	public void update(int d){
		for (int i = 0; i < novelty.size(); i++) {
			float newNovelty = novelty.get(i).getValue()*(1-coverage.get(i)[d].getValue());
			novelty.get(i).setValue(newNovelty);
		}
		
	}
	
	public void update(List<FlatAspect> importance, List<FlatAspect[]> coverage,List<FlatAspect> novelty){
		this.importance = importance;
		this.coverage = coverage;
		this.novelty = novelty;
	}

	@Override
	public float score(int d) {
		float s = div(d);
		update(d);
		return s;
	}

}
