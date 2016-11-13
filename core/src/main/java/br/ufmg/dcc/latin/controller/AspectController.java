package br.ufmg.dcc.latin.diversity;

import br.ufmg.dcc.latin.feedback.Feedback;

public interface AspectManager {
	
	public void miningDiversityAspects(Feedback[] feedbacks);
	public void miningProportionalAspects(Feedback[] feedbacks);
}
