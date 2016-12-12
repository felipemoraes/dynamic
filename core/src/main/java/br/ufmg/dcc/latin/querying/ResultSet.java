package br.ufmg.dcc.latin.querying;

import org.apache.lucene.search.TopDocs;

public class ResultSet{

	public int[] docids;
	public double[] scores;
	public String[] docnos;
	public String[] docsContent;
	public TopDocs topDocs;
	public ResultSet(int size){
		docids = new int[size];
		scores = new double[size];
		docnos = new String[size];
		docsContent = new String[size];
		
	}
	
	public ResultSet(){
	}

}
