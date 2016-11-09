package br.ufmg.dcc.latin.scoring.diversity;

import br.ufmg.dcc.latin.diversity.Aspect;
import br.ufmg.dcc.latin.scoring.DiversityScorer;

public class xQuADs  extends DiversityScorer {
	
	Aspect[] importance;
	Aspect[][] coverage;
	float[] novelty;
	 

	
	public xQuADs(Aspect[] importance, Aspect[][] coverage, float[] novelty){
		this.importance = importance;
		this.coverage = coverage;
		this.novelty = novelty;
	}
	
	@Override
	public float div(int d){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity += importance[i].getValue()*coverage[d][i].getValue()*novelty[d];
		}
		return diversity;
	}
	

	public void update(float[] sims){
		
 		for (int i = 0; i < novelty.length; i++) {
			float newNovelty = novelty[i]*(1-sims[i]);
			novelty[i] = newNovelty;
		}
		
	}
	
	
	public void update(Aspect[] importance, Aspect[][] coverage){
		this.importance = importance;
		this.coverage = coverage;
	}

	@Override
	public float score(int d) {
		float s = div(d);
		update(d);
		return s;
	}
}
