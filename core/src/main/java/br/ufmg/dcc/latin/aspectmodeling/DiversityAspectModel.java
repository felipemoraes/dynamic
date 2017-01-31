package br.ufmg.dcc.latin.aspectmodeling;

public interface DiversityAspectModel {
	public double[] getAspectFlatCoverage(String aspectId);
	
	public double[][] getAspectHierchicalCoverage(String aspectId);
}
