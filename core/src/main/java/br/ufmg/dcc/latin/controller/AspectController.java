package br.ufmg.dcc.latin.controller;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.scoring.Scorer;

public interface AspectController {
	
	public void miningDiversityAspects(Feedback[] feedbacks);
	public void miningProportionalAspects(Feedback[] feedbacks);
	public void mining(Feedback[] feedback, Scorer scorer);
}
