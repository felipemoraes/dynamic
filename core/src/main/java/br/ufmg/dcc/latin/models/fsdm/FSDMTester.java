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

public class FSDMTester {
	
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	private FSDM fsdm = new FSDM();
	private HashMap<String, JsonNode> unigramsByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> bigramsByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> fieldsStatsByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> uCollFreqByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, JsonNode> bCollFreqByQuery = new HashMap<String, JsonNode>();
	private HashMap<String, ArrayList<Document>> docsByQuery = new HashMap<String, ArrayList<Document>>();
	
	private ObjectMapper objMapper = new ObjectMapper();
	
	
	public FSDMTester(){
		
	}
	
	public void loadAllDataToMemory(String folderPath, ArrayList<String> queryIds){
		int i = 0;
		for(String queryId : queryIds){
			try {
				loadFileToMemory(folderPath, queryId);

			} catch (IOException e) {
				System.out.println("Erro: "+queryId);
			}
			i++;
		}
		
		System.out.println("Queries loaded: "+i);
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
	
	/**
	 * Returns a reordered ranking using one of the tree models of FSDM: unigram, ordered bigrams or unordered bigrams
	 * @param model name of the model to use: "unigrams", "bigrams" or "wbigrams"
	 * @param weights array of weights to use for each field
	 * @param Query ID of the initial ranking to reorder
	 * @return Hashmap of the reordered ranking, URL as key and score as value
	 */
	public HashMap<String, Double> rerankingQueryResultWithFsdmModel(String model, double[] weights, String queryId){
		fsdm.setFieldWeight(model, "name", weights[0]);
		fsdm.setFieldWeight(model, "content", weights[1]);
		fsdm.setFieldWeight(model, "categories", weights[2]);
		fsdm.setFieldWeight(model, "similar_entities", weights[3]);
		fsdm.setFieldWeight(model, "related_entities", weights[4]);
		//fsdm.setFieldWeight(model, "url", weights[5]);
		//fsdm.setFieldWeight(model, "classes", weights[6]);
		//fsdm.setFieldWeight(model, "all", weights[7]);
		
		HashMap<String, Double> newRanking = new HashMap<String, Double>();
		
		JsonNode unigrams = unigramsByQuery.get(queryId);
		JsonNode bigrams = bigramsByQuery.get(queryId);
		JsonNode fieldsStats = fieldsStatsByQuery.get(queryId);
		JsonNode uCollFreq = uCollFreqByQuery.get(queryId);
		JsonNode bCollFreq = bCollFreqByQuery.get(queryId);
		
		if(model.equals("unigrams")){
			for(Document doc : docsByQuery.get(queryId)){
				newRanking.put(doc.getUrl(), fsdm.unigramScore(unigrams, doc, fieldsStats, uCollFreq));
			}
		}
		else if(model.equals("bigrams")){
			for(Document doc : docsByQuery.get(queryId)){
				newRanking.put(doc.getUrl(), fsdm.bigramScore(bigrams, doc, fieldsStats, bCollFreq));
			}
		}
		else{
			for(Document doc : docsByQuery.get(queryId)){
				newRanking.put(doc.getUrl(), fsdm.windowBigramScore(bigrams, doc, fieldsStats));
			}
		}
		
		HashMap<String, Double> orderedFSDMRanking = (HashMap<String, Double>) MapUtil.sortByValue(newRanking);
		
		return orderedFSDMRanking;
	}
	
	/**
	 * Reorder all queries using one of the models of FSDM: unigrams, ordered bigrams or unordered bigrams
	 * @param model name of the model to use: "unigrams", "bigrams" or "wbigrams"
	 * @param weights array of field weights
	 * @return An HashMap with queryId as key and arrayList of reordered URL docs
	 */
	public HashMap<String, ArrayList<String>> experimentFieldWeights(ArrayList<String> queryIds, String model, double[] weights){
		
		HashMap<String, ArrayList<String>> rankings = new HashMap<String, ArrayList<String>>();
		
		for(String queryId : queryIds){
			
			HashMap<String, Double> newRanking = rerankingQueryResultWithFsdmModel(model, weights, queryId);
			//I need only the URL, so create an ArrayList with ordered URLs
			ArrayList<String> urls = new ArrayList<String>(); 
			for(Map.Entry<String, Double> rankingEntry : newRanking.entrySet()){
				urls.add(rankingEntry.getKey());
			}
			
			rankings.put(queryId, urls);
		}
		
		return rankings;
	}
	
	/**
	 * Returns a reordered ranking using FSDM
	 * @param Query ID of the initial ranking to reorder
	 * @param alphaWeights array of weights to use for each n-gram model
	 * @return Hashmap of the reordered ranking, URL as key and score as value
	 */
	public HashMap<String, Double> rerankingQueryResultWithFsdm(String queryId, double[] alphaWeights, double[] unigramsWeights, double[] bigramsWeights, double[] wbigramsWeights){
		fsdm.setAllUnigramFieldsWeights(unigramsWeights);
		fsdm.setAllBigramFieldsWeights(bigramsWeights);
		fsdm.setAllWbigramFieldsWeights(wbigramsWeights);
		
		fsdm.setUnigramAlphaWeight(alphaWeights[0]);
		fsdm.setBigramAlphaWeight(alphaWeights[1]);
		fsdm.setWbigramAlphaWeight(alphaWeights[2]);
		
		HashMap<String, Double> newRanking = new HashMap<String, Double>();
		
		JsonNode unigrams = unigramsByQuery.get(queryId);
		JsonNode bigrams = bigramsByQuery.get(queryId);
		JsonNode fieldsStats = fieldsStatsByQuery.get(queryId);
		JsonNode uCollFreq = uCollFreqByQuery.get(queryId);
		JsonNode bCollFreq = bCollFreqByQuery.get(queryId);
		
		for(Document doc : docsByQuery.get(queryId)){
			newRanking.put(doc.getUrl(), fsdm.finalScore(unigrams, bigrams, doc, fieldsStats, uCollFreq, bCollFreq));//fsdm.unigramScore(unigrams, doc, fieldsStats, uCollFreq));
		}
		
		HashMap<String, Double> orderedFSDMRanking = (HashMap<String, Double>) MapUtil.sortByValue(newRanking);
		
		return orderedFSDMRanking;
	}
	
	/**
	 * Reorder queries using FSDM
	 * @param weights array of weights for each model
	 * @return An HashMap with queryId as key and arrayList of reordered URL docs
	 */	
	public HashMap<String, ArrayList<String>> experimentModelWeights(ArrayList<String> queryIds, double[] alphaWeights, double[] unigramsWeights, double[] bigramsWeights, double[] wbigramsWeights){
		
		HashMap<String, ArrayList<String>> rankings = new HashMap<String, ArrayList<String>>();
		
		for(String queryId : queryIds){
			
			HashMap<String, Double> newRanking = rerankingQueryResultWithFsdm(queryId, alphaWeights, unigramsWeights, bigramsWeights, wbigramsWeights);
			//I need only the URL, so create an ArrayList with ordered URLs
			ArrayList<String> urls = new ArrayList<String>(); 
			for(Map.Entry<String, Double> rankingEntry : newRanking.entrySet()){
				urls.add(rankingEntry.getKey());
			}
			
			rankings.put(queryId, urls);
		}
		
		return rankings;
	}
	
	public HashMap<String, JsonNode> getUnigramsByQuery(){
		return unigramsByQuery;
	}
	public HashMap<String, JsonNode> getBigramsByQuery(){
		return bigramsByQuery;
	}
	public HashMap<String, JsonNode> getFieldsStatsByQuery(){
		return fieldsStatsByQuery;
	}
	public HashMap<String, JsonNode> getUnigramsCollFreqByQuery(){
		return uCollFreqByQuery;
	}
	public HashMap<String, JsonNode> getBigramsCollFreqByQuery(){
		return bCollFreqByQuery;
	}
	public HashMap<String, ArrayList<Document>> getDocsByQuery(){
		return docsByQuery;
	}
}
