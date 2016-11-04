package br.ufmg.dcc.latin.scoring.diversity;


import br.ufmg.dcc.latin.diversity.Aspect;
import br.ufmg.dcc.latin.scoring.DiversityScorer;


public class xQuAD extends DiversityScorer{
	
	 Aspect[] importance;
	 Aspect[][] coverage;
	 Aspect[] novelty;
	 
	public xQuAD(Aspect[] importance, Aspect[][] coverage, Aspect[] novelty){
		this.importance = importance;
		this.coverage = coverage;
		this.novelty = novelty;
	}
	
	@Override
	public float div(int d){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity += importance[i].getValue()*coverage[d][i].getValue()*novelty[i].getValue();
		}
		return diversity;
	}
	
	@Override
	public void update(int d){
		for (int i = 0; i < novelty.length; i++) {
			float newNovelty = novelty[i].getValue()*(1-coverage[d][i].getValue());
			novelty[i].setValue(newNovelty);
		}
		
	}
	
	public void update(Aspect[] importance, Aspect[][] coverage, Aspect[] novelty){
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
