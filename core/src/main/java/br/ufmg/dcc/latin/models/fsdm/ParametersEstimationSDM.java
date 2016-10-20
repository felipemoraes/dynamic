package br.ufmg.dcc.latin.models.fsdm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParametersEstimationSDM {
	
	private SDMTester tester;
	private Evaluator evaluator = new Evaluator();
	
	
	public ParametersEstimationSDM(String queriesFilePath, String qrelsFilePath, String dataFolderPath){
		tester = new SDMTester(queriesFilePath);
		evaluator.parseQrelsFile(qrelsFilePath);
		tester.loadAllDataToMemory(dataFolderPath);
	}
	
	public void coordinateAscentForModels(){
		
		double[] values = new double[]{0.1, 0.1, 0.1};
		int index = 0;
		double step = 0.05;
		double bestMap = evaluator.map(tester.experimentModelWeights(values));
		double bestParam = values[index];
		double newMap = 0.0;
		double bestForNow = 0.0;
		boolean runAllParams = true;
		
		System.out.println("Initial map: "+bestMap);
		
		int k = 0;
		while(runAllParams){//for(int k=0; k<=15; k++){
			k++;
			System.out.println(k+"-----------------------------------------------");
			bestForNow = 0;
			for(int j=0; j<values.length; j++){
				index = j;
				bestParam = values[index];
				boolean shouldContinue = true;
				while(shouldContinue){//for(int i=0; i<=20; i++){
					values[index] += step;
					
					//System.out.print("["+dateFormat.format(new Date())+"] "+i+": ");
					for(double v : values)
						System.out.print(round(v, 2)+" ");
					System.out.print(" - ");
					
					HashMap<String, ArrayList<String>> rankings = tester.experimentModelWeights(values);
					newMap = evaluator.map(rankings);
					if(newMap > bestMap){
						bestMap = newMap;
						bestParam = values[index];
					}
					else{
						shouldContinue = false;
					}
					System.out.println(newMap);
					
					if(newMap > bestForNow)
						bestForNow = newMap;
				}
				
				values[index] = bestParam;
			}
			if(bestForNow < bestMap || k > 10)
				runAllParams = false;
			System.out.println("Melhor nessa rodada: "+bestForNow+" global: "+bestMap);
		}
		
		System.out.print("melhores: ");
		for(double v : values)
			System.out.print(v+" ");
		System.out.print(" - ");
		System.out.println(bestMap);
		
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
}
