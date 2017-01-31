package br.ufmg.dcc.latin.baselineranker;

public class ResultList {
	public int[] docids;
	public String[] docnos;
	public double[] scores;
	public String topicId;
	
	public ResultList(int n){
		docids = new int[n];
		docnos = new String[n];
		scores = new double[n];
	}
}
