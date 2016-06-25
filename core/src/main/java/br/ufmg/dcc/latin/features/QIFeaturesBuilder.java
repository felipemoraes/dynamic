package br.ufmg.dcc.latin.features;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class QIFeaturesBuilder {
	
	private float[][] features;
	
	public QIFeaturesBuilder(){
		features = new float[500000][];
	
		
		try (BufferedReader br = new BufferedReader(new FileReader(ApplicationSetup.QUERY_INDEPENDENT_FILENAME))) {
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
