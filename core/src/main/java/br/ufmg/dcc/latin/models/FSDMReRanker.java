package br.ufmg.dcc.latin.models;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.lucene.queryparser.classic.ParseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufmg.dcc.latin.models.fsdm.Document;
import br.ufmg.dcc.latin.models.fsdm.FSDM;
import br.ufmg.dcc.latin.models.fsdm.QueryProcessor;
import br.ufmg.dcc.latin.search.elements.Feedback;

public class FSDMReRanker {

	
	private String folderPath = "data/";
	private QueryProcessor processor;
	private String[] docIds;
	private double[] relevance;

	
	HashSet<Integer> selected;
	
	public void create(String index, String query) throws IOException, ParseException {
		System.out.println("Not implemented");

	}
	
	public void create(String index, String query, String topicId) throws IOException, ParseException {
		ArrayList<String> fields = new ArrayList<String>();
		fields.add("text");
		if (processor == null) {
			processor = new QueryProcessor(true, index, fields);
		}
		processor.setIndexName(index);
		
		processor.saveJsonQueryResult(topicId, query, folderPath);

		
		FSDM fsdm = new FSDM();
		
		
		
		
		BufferedReader br = new BufferedReader(new FileReader(folderPath+"/"+topicId+".txt"));
		
		ObjectMapper objMapper = new ObjectMapper();
		
		JsonNode unigrams = objMapper.readTree(br.readLine());
		JsonNode bigrams = objMapper.readTree(br.readLine());
		JsonNode fieldsStats = objMapper.readTree(br.readLine());
		JsonNode uCollFreq = objMapper.readTree(br.readLine());
		JsonNode bCollFreq = objMapper.readTree(br.readLine());
					
		
		ArrayList<Document> docsList = new ArrayList<Document>();
		String line;
		while ((line = br.readLine()) != null) {
			docsList.add(objMapper.readValue(line, Document.class));
		}
		
		br.close();
	
		selected = new HashSet<Integer>();
		
		docIds = new String[docsList.size()];
		relevance = new double[docsList.size()];
		
		for(int i = 0 ; i < docIds.length; ++i){
			docIds[i] = docsList.get(i).getUrl();
			relevance[i] = fsdm.finalScore(unigrams, bigrams, docsList.get(i), fieldsStats, uCollFreq, bCollFreq);
		}
		
		
	}

	public Map<String, Double> get() {
		
		Map<String, Double> result = new HashMap<String, Double>();

		while (result.size() < 5 && selected.size() < docIds.length){
			int maxRank = -1;
			double maxScore = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < relevance.length; i++) {
				if (selected.contains(i)){
					continue;
				}
				if (maxScore < relevance[i]) {
					maxRank = i;
					maxScore = relevance[i];
				}
			}
			selected.add(maxRank);
			result.put(docIds[maxRank], maxScore);
			
		}
		return result;
	}

	public void update(Feedback[] feedback) {
		// TODO Auto-generated method stub

	}

}
