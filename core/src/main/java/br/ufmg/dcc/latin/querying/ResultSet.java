package br.ufmg.dcc.latin.querying;

import org.apache.lucene.search.TopDocs;

public class ResultSet{

	public int[] docids;
	public double[] scores;
	public String[] docnos;
	public int indices[];
	public TopDocs topDocs;
	
	public ResultSet(int size){
		docids = new int[size];
		scores = new double[size];
		docnos = new String[size];
		indices = new int[size];
	}
	
	public ResultSet(){
	}

}
