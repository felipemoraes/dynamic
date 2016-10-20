package br.ufmg.dcc.latin.models.fsdm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SDMTester {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	SDM sdm = new SDM();
	private QueryProcessor queryProcessor = new QueryProcessor();
	private HashMap<String, String> queries;
	
	private HashMap<String, JsonNode> unigramsByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> bigramsByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> fieldsStatsByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> uCollFreqByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> bCollFreqByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, ArrayList<Document>> docsByQuery = new HashMap<String, ArrayList<Document>>();
	
	private ObjectMapper objMapper = new ObjectMapper();
	
	public SDMTester(String queriesFilePath){
		queryProcessor.parseQueriesFile(queriesFilePath);
		queries = queryProcessor.getQueries();
	}
	
	public void loadAllDataToMemory(String folderPath){
		ArrayList<String> idsErrors = new ArrayList<String>(); 
		int i = 1;
		for(Map.Entry<String, String> entry : queries.entrySet()){
			String queryId = entry.getKey();
			String query = entry.getValue();
			
			System.out.println("["+dateFormat.format(new Date())+"] "+i+" "+query);
			i++;
			try {
				loadFileToMemory(folderPath, queryId);

			} catch (IOException e) {
				System.out.println("Erro: "+queryId);
				idsErrors.add(queryId);
			}
		}
		for(String id : idsErrors)
			queries.remove(id);
	}
	
	public void loadFileToMemory(String folderPath, String queryId) throws IOException{
		BufferedReader br = new BufferedReader(new FileReader(folderPath+"/"+queryId+".txt"));
		
		unigramsByQuery.put(queryId, objMapper.readTree(br.readLine()));
		bigramsByQuery.put(queryId, objMapper.readTree(br.readLine()));
		fieldsStatsByQuery.put(queryId, objMapper.readTree(br.readLine()));
		uCollFreqByQuery.put(queryId, objMapper.readTree(br.readLine()));
		bCollFreqByQuery.put(queryId, objMapper.readTree(br.readLine()));
					
		String line;
		ArrayList<Document> docsList = new ArrayList<Document>();
		while ((line = br.readLine()) != null) {
			docsList.add(objMapper.readValue(line, Document.class));
		}
		
		docsByQuery.put(queryId, docsList);
		
		br.close();
		
	}
	
	public HashMap<String, Double> rerankingQueryResultWithSdm(double[] weights, String queryId){
		sdm.setUnigramModelWeights(weights[0]);
		sdm.setBigramModelWeights(weights[1]);
		sdm.setWbigramModelWeights(weights[2]);
		
		HashMap<String, Double> newRanking = new HashMap<String, Double>();
		
		JsonNode unigrams = unigramsByQuery.get(queryId);
		JsonNode bigrams = bigramsByQuery.get(queryId);
		JsonNode fieldsStats = fieldsStatsByQuery.get(queryId);
		JsonNode uCollFreq = uCollFreqByQuery.get(queryId);
		JsonNode bCollFreq = bCollFreqByQuery.get(queryId);
		
		double i = 1000;
		for(Document doc : docsByQuery.get(queryId)){
			newRanking.put(doc.getUrl(), sdm.finalScore2(unigrams, bigrams, doc, fieldsStats, uCollFreq, bCollFreq));
			i--;
		}
		
		HashMap<String, Double> orderedFSDMRanking = (HashMap<String, Double>) MapUtil.sortByValue(newRanking);
		/*for(Entry<String, Double> entry : orderedFSDMRanking.entrySet()){
			String url = entry.getKey();
			Double score = entry.getValue();
			System.out.println(url+" - "+score);
		}*/
		return orderedFSDMRanking;
	}
	
	public HashMap<String, ArrayList<String>> experimentModelWeights(double[] weights){
		
		HashMap<String, ArrayList<String>> rankings = new HashMap<String, ArrayList<String>>();
		
		for(Map.Entry<String, String> entry : queries.entrySet()){
			String queryId = entry.getKey();
			//String query = entry.getValue();
			
			HashMap<String, Double> newRanking = rerankingQueryResultWithSdm(weights, queryId);
			//I need only the URL, so create an ArrayList with ordered URLs
			ArrayList<String> urls = new ArrayList<String>(); 
			for(Map.Entry<String, Double> rankingEntry : newRanking.entrySet()){
				urls.add(rankingEntry.getKey());
			}
			
			rankings.put(queryId, urls);
		}
		
		return rankings;
	}

}
