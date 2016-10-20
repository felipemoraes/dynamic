package br.ufmg.dcc.latin.models.fsdm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class FSDMCrossValidation {
	
	
	private Evaluator evaluator = new Evaluator();
	
	public FSDMCrossValidation(){
		
	}
	
	public ArrayList<String> loadQueries(String queriesFilePath){
		QueryProcessor queryProcessor = new QueryProcessor();
		HashMap<String, String> queriesMap = new HashMap<String, String>();
		
		queryProcessor.parseQueriesFile(queriesFilePath);
		queriesMap = queryProcessor.getQueries();
		
		return new ArrayList<String>(queriesMap.keySet());
	}
	
	public ArrayList<ArrayList<String>> createFolds(ArrayList<String> queryIds){
		int numFolds = 5;
		
		//Collections.shuffle(queryIds, new Random(System.nanoTime()));
		int chunkSize = (queryIds.size() - (queryIds.size() % numFolds)) / numFolds;
		ArrayList<ArrayList<String>> folds = new ArrayList<ArrayList<String>>(); 
		
		for(int i=0; i<5; i++){
			int from = chunkSize * i;
			int to = from + chunkSize;
			folds.add(new ArrayList<String>(queryIds.subList(from, to)));
		}
		
		return folds;
	}
	
	public double macroAverage(ArrayList<String> queryIds, ArrayList<ArrayList<String>> folds, double[] alphaWeights, double[] uWeights, double[] bWeights, double[] wWeights){
		double averageMap = 0;
		
		FSDMTester tester = new FSDMTester();
		tester.loadAllDataToMemory("queries_responses_fsdm2/", queryIds);
		
		for(ArrayList<String> queryIdsFold : folds){
			averageMap += evaluator.map(tester.experimentModelWeights(queryIdsFold, alphaWeights, uWeights, bWeights, wWeights));
		}
		
		averageMap = averageMap / (double) folds.size();
		
		return averageMap;
	}
	
	public double macroAverageByQueryType(String queriesFilePath, String qrelsFilePath, double[] alphaWeights, double[] uWeights, double[] bWeights, double[] wWeights){
		evaluator.parseQrelsFile(qrelsFilePath);
		ArrayList<String> queryIds = loadQueries(queriesFilePath);
		
		ArrayList<ArrayList<String>> folds = createFolds(queryIds);
		return macroAverage(queryIds, folds, alphaWeights, uWeights, bWeights, wWeights);
	}
	
	public double macroAverageAllQueriesType(String[] types, String queriesFolderPath, String qrelsFilePath, HashMap<String, double[]> alphaWeights, HashMap<String, double[]> uWeights, HashMap<String, double[]> bWeights, HashMap<String, double[]> wWeights){
		double finalValue = 0;
		
		//create the folds for each query type
		ArrayList<String> allQueryIds = new ArrayList<String>();
		HashMap<String, ArrayList<String>> queryIdsByType = new HashMap<String, ArrayList<String>>(); 
		HashMap<String, ArrayList<ArrayList<String>>> foldsByType = new HashMap<String, ArrayList<ArrayList<String>>>();
		for(String type : types){
			ArrayList<String> queryIds = loadQueries(queriesFolderPath+"/queries_"+type+".txt");
			queryIdsByType.put(type, queryIds);
			allQueryIds.addAll(queryIds);
			foldsByType.put(type, createFolds(queryIds));
		}
		
		evaluator.parseQrelsFile(qrelsFilePath);
		FSDMTester tester = new FSDMTester();
		tester.loadAllDataToMemory("queries_responses_fsdm2/", allQueryIds);
		
		
		for(int i=0; i<5; i++){
			double averageMap = 0;
			for(String type : types){
				averageMap += evaluator.map(tester.experimentModelWeights(foldsByType.get(type).get(i), alphaWeights.get(type), uWeights.get(type), bWeights.get(type), wWeights.get(type)));
			}
			averageMap = averageMap / (double) types.length;
			finalValue += averageMap;
		}
		finalValue = finalValue / (double) 5;
		
		
		return finalValue;
	}
}
