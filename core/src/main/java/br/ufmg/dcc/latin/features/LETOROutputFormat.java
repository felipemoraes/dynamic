package br.ufmg.dcc.latin.features;

import java.io.FileNotFoundException;
import java.io.PrintWriter;



public class LETOROutputFormat {
	private PrintWriter writer;
	
	public LETOROutputFormat(){
		try {
			writer = new PrintWriter(ApplicationSetup.OUTPUT_FILENAME);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write(int queryId, float[] features){
		String line = "-1 qid:" + queryId + " ";
	
		for (int i = 0; i < features.length; i++) {
			line += i+1 + ":" + features[i] + " ";
		}
		writer.write(line+"\n");
	}

}
