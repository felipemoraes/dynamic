package br.ufmg.dcc.latin.scoring;

public class DiversityScorer implements Scorer {

	@Override
	public float score(int d) {
		float s = div(d);
		update(d);
		return s;
	}
	
	public float div(int d){
		return 0;
	}
	
	public void update(int d){
		
	}

}
