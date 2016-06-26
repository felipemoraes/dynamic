package br.ufmg.dcc.latin.features;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class QueryIndependentFeatures {
	
	private float[][] features;
	
	//TODO handles names
	
	public QueryIndependentFeatures(String featuresFilename){
		features = new float[500000][];
	
		
		try (BufferedReader br = new BufferedReader(new FileReader(featuresFilename))) {
		    String line;
		    while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(" ");
				int n = splitLine.length -1;
				float[] scores = new float[n];
				for (int i = 0; i < scores.length; i++) {
					scores[i] = Float.parseFloat(splitLine[i+1]);
				}
				int docId = Integer.parseInt(splitLine[0]); 
				features[docId] = scores;
				
		    }
		    
		    
		    
		   
		    
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}

		
	}
	
	public float[] getDocFeatures(int docId) {
		return features[docId];
	}
	

}
