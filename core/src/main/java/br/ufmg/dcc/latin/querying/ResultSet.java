package br.ufmg.dcc.latin.querying;

public class ResultSet{

	public int[] docids;
	public float[] scores;
	public String[] docnos;
	public String[] docsContent;
	
	public ResultSet(int size){
		docids = new int[size];
		scores = new float[size];
		docnos = new String[size];
		docsContent = new String[size];
	}
	
	public ResultSet(){
	}

}
