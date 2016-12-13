package br.ufmg.dcc.latin.querying;

public class ResultSet{

	public int[] docids;
	public double[] scores;
	public String[] docnos;
	public int indices[];
	
	public ResultSet(int size){
		docids = new int[size];
		scores = new double[size];
		docnos = new String[size];
		indices = new int[size];
	}
	
	public ResultSet(){
	}

}
