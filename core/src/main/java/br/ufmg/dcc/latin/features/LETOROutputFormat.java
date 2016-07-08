package br.ufmg.dcc.latin.features;

import java.io.FileNotFoundException;
import java.io.PrintWriter;



public class LETOROutputFormat {
	
	private PrintWriter writer;
	
	public LETOROutputFormat(String fileName){
		try {
			writer = new PrintWriter(fileName);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(int queryId, float[][] features, int[] docId){
		
		for (int i = 0; i < docId.length; i++) {
			
			String line = "-1 qid:" + queryId + " ";
			
			for (int j = 0; j < features[i].length; j++) {
				line += j+1 + ":" + features[i][j] + " ";
			}
			writer.write(line+ "# " + docId[i] +"\n");
			
			
		}

	}
	public void close(){
		writer.close();
	}
	public void write(int queryId, float[][] features, String[] docId){
		for (int i = 0; i < docId.length; i++) {
			
			String line = "-1 qid:" + queryId + " ";
			
			for (int j = 0; j < features[i].length; j++) {
				line += j+1 + ":" + features[i][j] + " ";
			}
			writer.write(line+ "#docid = " + docId[i] +"\n");
			
			
		}
	}
	
	public void write(int queryId, float[] features, String[] docId){
		for (int i = 0; i < docId.length; i++) {
			
			String line = "-1 qid:" + queryId + " ";
			
			line +=  "1:" + features[i] + " ";
			
			writer.write(line+ "#docid = " + docId[i] +"\n");
			
			
		}
	}
}
