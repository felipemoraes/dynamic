package br.ufmg.dcc.latin.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.math3.stat.StatUtils;

public class QueryIndependentFeatures {
	double[][] features;
	
	public QueryIndependentFeatures(String index, int n){
		String featuresFile = "../etc/" +  index + "_qif";
		features = new double[n][17];
		
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(featuresFile));
		    while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ");
		    	int docid = Integer.parseInt(splitLine[0]);
		    	for (int i = 1; i < splitLine.length; i++) {
					String[] splitFeature = splitLine[i].split(":");
					double feature = Double.parseDouble(splitFeature[1]);
					features[docid][i-1] = feature;
				}
		    }
		    br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public double[] getFeatures(int docid){
		return features[docid];
	}
	
	public double[][] getStatistics(int[] docids){
		double[][] statistics = new double[3][17];
		
		for (int j = 0; j < 17; j++) {
			double[] featureValues = new double[docids.length];
			for (int i = 0; i < docids.length; i++) {
				if (features[docids[i]] == null) {
					continue;
				}
				featureValues[i] = features[docids[i]][j];
			}
			statistics[0][j] = StatUtils.mean(featureValues);
			statistics[1][j] = StatUtils.percentile(featureValues, 0.5);
			statistics[2][j] = StatUtils.variance(featureValues);
		}
		
		return statistics;
	}
}
