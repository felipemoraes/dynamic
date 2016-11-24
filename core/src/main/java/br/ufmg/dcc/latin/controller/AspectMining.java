package br.ufmg.dcc.latin.controller;

import br.ufmg.dcc.latin.feedback.Feedback;

public interface AspectMining {
	public void miningDiversityAspects(Feedback[] feedbacks);
	public void miningProportionalAspects(Feedback[] feedbacks);
	public float[][] getCoverage();
	public float[] getImportance();
	public float[] getNovelty();
	
}
