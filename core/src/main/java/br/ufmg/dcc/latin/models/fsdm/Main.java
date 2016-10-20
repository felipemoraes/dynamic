package br.ufmg.dcc.latin.models.fsdm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Main {
	
	String[] types = new String[]{"semsearch_es", "listsearch", "inexld", "qald2"};
	
	String queriesFolderPath = "../ranking_queries/letor/queries/";
	String qrelsFolderPath = "../ranking_queries/letor/queries/";
	String fsdmDataFolderPath = "queries_responses_fsdm2/";
	String sdmDataFolderPath = "queries_responses_sdm2/";
	HashMap<String, double[]> alphaWeights = new HashMap<String, double[]>();
	HashMap<String, double[]> uWeights = new HashMap<String, double[]>();
	HashMap<String, double[]> bWeights = new HashMap<String, double[]>();
	HashMap<String, double[]> wWeights = new HashMap<String, double[]>();

	public static void main(String[] args) {
		
		/*Evaluator ev = new Evaluator();
		ev.parseQrelsFile("../ranking_queries/letor/queries/qrels.txt");
		ev.parseTrecEvalFile("../ranking_queries/letor/training_test/final_rankings/bm25_teste");
		System.out.println(ev.map());*/
		
		Main main = new Main();
		
		main.processOneQueryAndSaveResult("SemSearch_ES-95", "MADRID", "queries_responses_fsdm2/");
		
		//execute and save all initial rankings as JSON files
		//main.processQueriesAndSaveResultsFSDM();
		
		//main.estimateFsdmWs("semsearch_es", "unigrams");
		//main.estimateFsdmWs("semsearch_es", "bigrams");
		//main.estimateFsdmWs("semsearch_es", "wbigrams");
		//main.estimateFsdmAlphas("semsearch_es");
		
		//main.estimateFsdmWs("listsearch", "unigrams");
		//main.estimateFsdmWs("listsearch", "bigrams");
		//main.estimateFsdmWs("listsearch", "wbigrams");
		//main.estimateFsdmAlphas("listsearch");
		
		//main.estimateFsdmWs("inexld", "unigrams");
		//main.estimateFsdmWs("inexld", "bigrams");
		//main.estimateFsdmWs("inexld", "wbigrams");
		//main.estimateFsdmAlphas("inexld");
		
		//main.estimateFsdmWs("qald2", "unigrams");
		//main.estimateFsdmWs("qald2", "bigrams");
		//main.estimateFsdmWs("qald2", "wbigrams");
		//main.estimateFsdmAlphas("qald2");
		
		//main.macroAverageByType();
		
		//main.macroAveragesAllTypes();
		//main.teste();

	}
	
	public Main(){
		//weights from paper
		/*alphaWeights.put("semsearch_es", new double[]{0.6, 0.2, 0.2});
		uWeights.put("semsearch_es", new double[]{0.25, 0.4, 0.05, 0.25, 0.05});
		bWeights.put("semsearch_es", new double[]{0.3, 0.2, 0.1, 0.4, 0.0});
		wWeights.put("semsearch_es", new double[]{0.25, 0.25, 0.1, 0.4, 0.0});
		
		alphaWeights.put("listsearch", new double[]{0.6, 0.2, 0.2});
		uWeights.put("listsearch", new double[]{0.0, 0.4, 0.5, 0.05, 0.05});
		bWeights.put("listsearch", new double[]{0.0, 0.25, 0.4, 0.1, 0.25});
		wWeights.put("listsearch", new double[]{0.05, 0.3, 0.4, 0.1, 0.15});
		
		alphaWeights.put("inexld", new double[]{0.7, 0.15, 0.15});
		uWeights.put("inexld", new double[]{0.1, 0.5, 0.25, 0.1, 0.05});
		bWeights.put("inexld", new double[]{0.15, 0.2, 0.35, 0.25, 0.05});
		wWeights.put("inexld", new double[]{0.15, 0.3, 0.3, 0.2, 0.05});
		
		alphaWeights.put("qald2", new double[]{0.5, 0.3, 0.2});
		uWeights.put("qald2", new double[]{0.0, 0.3, 0.2, 0.15, 0.35});
		bWeights.put("qald2", new double[]{0.0, 0.1, 0.45, 0.2, 0.25});
		wWeights.put("qald2", new double[]{0.0, 0.15, 0.35, 0.2, 0.3});*/
		
		//weights estimated from me
		alphaWeights.put("semsearch_es", new double[]{0.63, 0.15, 0.22});
		uWeights.put("semsearch_es", new double[]{0.3, 0.28, 0.12, 0.21, 0.09});
		bWeights.put("semsearch_es", new double[]{0.29, 0.18, 0.17, 0.17, 0.19});
		wWeights.put("semsearch_es", new double[]{0.35, 0.25, 0.09, 0.16, 0.15});
		

		
	}
	
	public void teste(){
		FSDMTester teste = new FSDMTester();
		try {
			teste.loadFileToMemory("queries_responses_fsdm2/", "TREC_Entity-14");
			HashMap<String, Double> results = teste.rerankingQueryResultWithFsdm("TREC_Entity-14", alphaWeights.get("listsearch"), uWeights.get("listsearch"), bWeights.get("listsearch"), wWeights.get("listsearch"));
			for(Entry<String, Double> entry : results.entrySet()){
				System.out.println(entry.getKey()+" - "+entry.getValue());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void processQueriesAndSaveResultsFSDM(){
		ArrayList<String> fields = new ArrayList<String>();
		
		//FSDM
		
		fields.add("text");

		
		QueryProcessor processor = new QueryProcessor(true, "dbpedia_fsdm2", fields);
		processor.processAndSaveAllQueryResults("../ranking_queries/letor/queries/queries2.txt", "queries_responses_fsdm/");
	}
	
	public void processOneQueryAndSaveResult(String queryId, String query, String folderPath){
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("name");
		fields.add("content");
		fields.add("categories");
		fields.add("similar_entities");
		fields.add("related_entities");
		
		QueryProcessor processor = new QueryProcessor(true, "dbpedia_fsdm2", fields);
		processor.saveJsonQueryResult(queryId, query, folderPath);
	}
	
	public void estimateFsdmWs(String queryType, String model){
		String queriesFilePath = queriesFolderPath+"queries_"+queryType+".txt";
		String qrelsFilePath = qrelsFolderPath+"qrels_"+queryType+".txt";
		
		ParametersEstimationFSDM estimation = new ParametersEstimationFSDM(queriesFilePath, qrelsFilePath, fsdmDataFolderPath);
		//estimation.adjustJSONs();
		estimation.coordinateAscentForWs(model);
	}
	
	public void estimateFsdmAlphas(String queryType){
		String queriesFilePath = queriesFolderPath+"queries_"+queryType+".txt";
		String qrelsFilePath = qrelsFolderPath+"qrels_"+queryType+".txt";
		
		ParametersEstimationFSDM estimation = new ParametersEstimationFSDM(queriesFilePath, qrelsFilePath, fsdmDataFolderPath);
		estimation.coordinateAscentForAlphas(uWeights.get(queryType), bWeights.get(queryType), wWeights.get(queryType));
	}
	
	public void macroAverageByType(){
		for(String type : types){
			FSDMCrossValidation fsdmCV = new FSDMCrossValidation();
			String queriesFilePath = queriesFolderPath+"queries_"+type+".txt";
			String qrelsFilePath = qrelsFolderPath+"qrels_"+type+".txt";
			System.out.print(type+" - ");
			System.out.println(fsdmCV.macroAverageByQueryType(queriesFilePath, qrelsFilePath, alphaWeights.get(type), uWeights.get(type), bWeights.get(type), wWeights.get(type)));
		}
	}
	
	public void macroAveragesAllTypes(){
		FSDMCrossValidation fsdmCV = new FSDMCrossValidation();
		String qrelsFilePath = qrelsFolderPath+"qrels2.txt";
		System.out.println(fsdmCV.macroAverageAllQueriesType(types, queriesFolderPath, qrelsFilePath, alphaWeights, uWeights, bWeights, wWeights));
	}

}
