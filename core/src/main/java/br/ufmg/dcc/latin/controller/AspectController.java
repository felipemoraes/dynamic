package br.ufmg.dcc.latin.controller;

import br.ufmg.dcc.latin.feedback.Feedback;

public interface AspectController {
	public void miningDiversityAspects(Feedback[] feedbacks);
	public void miningProportionalAspects(Feedback[] feedbacks);
}
