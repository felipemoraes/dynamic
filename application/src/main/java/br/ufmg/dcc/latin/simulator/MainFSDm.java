package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import br.ufmg.dcc.latin.models.Baseline;
import br.ufmg.dcc.latin.models.fsdm.Document;
import br.ufmg.dcc.latin.models.fsdm.FSDM;
import br.ufmg.dcc.latin.models.fsdm.MapUtil;
import br.ufmg.dcc.latin.models.fsdm.QueryProcessor;
import br.ufmg.dcc.latin.search.elements.Feedback;
import br.ufmg.dcc.latin.searcher.WeightingModule;
import br.ufmg.dcc.latin.searcher.es.models.LMDirichlet;

public class MainFSDM {

	public static void main(String[] args) throws FileNotFoundException, IOException {
    	String runId = "data/fsdm";
    	
    	
    	
    	
    	String topicsFile = "src/main/resources/topics_domain_2016.txt";
    	
    	String folderPath = "data/";
    	//xQuADiTFIDF xQuADi = new xQuADiTFIDF();
    	Baseline baseline = new Baseline();
    	
		
		DDSimulator simulator = new DDSimulator("src/main/resources/truth_data_2016.txt");
		
		//WeightingModule.changeWeightingModel("ebola_2016_fsdm", new LMDirichlet(2500.0));
		//WeightingModule.changeWeightingModel("polar_2016_fsdm", new  LMDirichlet(2500.0));
		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {

		    	String[] splitLine = line.split(" ",3);
	        	String indexName = splitLine[0];
	        	if (indexName.equals("Ebola")) {
	        		indexName = "ebola_2016_fsdm";
				} else if (indexName.equals("Polar")){
					indexName = "polar_2016_fsdm";
				}
	        	
	        	String topicId = splitLine[1];
	        	//if (!topicId.equals("DD16-18")){
	        	//	continue;
	        	//}
	        	
	    		String query = StringEscapeUtils.escapeJava(splitLine[2]);
	    		query = query.replaceAll("/", " ");
	    
	    		System.out.println(topicId);
	    		
	    		ArrayList<String> fields = new ArrayList<String>();
	    		fields.add("text");
	    		

	    		QueryProcessor processor = new QueryProcessor(true, indexName, fields);
	    		processor.saveJsonQueryResult(topicId, query, folderPath);

	    		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	    		
	    		FSDM fsdm = new FSDM();
	    		HashMap<String, JsonNode> unigramsByQuery = new HashMap<String, JsonNode>();
	    		HashMap<String, JsonNode> bigramsByQuery = new HashMap<String, JsonNode>();
	    		HashMap<String, JsonNode> fieldsStatsByQuery = new HashMap<String, JsonNode>();
	    		HashMap<String, JsonNode> uCollFreqByQuery = new HashMap<String, JsonNode>();
	    		HashMap<String, JsonNode> bCollFreqByQuery = new HashMap<String, JsonNode>();
	    		HashMap<String, ArrayList<Document>> docsByQuery = new HashMap<String, ArrayList<Document>>();
	    		
	    		ObjectMapper objMapper = new ObjectMapper();
	    		
	    		BufferedReader br_local = new BufferedReader(new FileReader(folderPath+"/"+topicId+".txt"));
	    		
	    		unigramsByQuery.put(topicId, objMapper.readTree(br_local.readLine()));
	    		bigramsByQuery.put(topicId, objMapper.readTree(br_local.readLine()));
	    		fieldsStatsByQuery.put(topicId, objMapper.readTree(br_local.readLine()));
	    		uCollFreqByQuery.put(topicId, objMapper.readTree(br_local.readLine()));
	    		bCollFreqByQuery.put(topicId, objMapper.readTree(br_local.readLine()));
	    					
	    		
	    		ArrayList<Document> docsList = new ArrayList<Document>();
	    		String line1;
	    		while ((line1 = br_local.readLine()) != null) {
	    			docsList.add(objMapper.readValue(line1, Document.class));
	    		}
	    		
	    		docsByQuery.put(topicId, docsList);
	    		
	    		br_local.close();
	    		
	    		HashMap<String, Double> newRanking = new HashMap<String, Double>();
	    		
	    		JsonNode unigrams = unigramsByQuery.get(topicId);
	    		JsonNode bigrams = bigramsByQuery.get(topicId);
	    		JsonNode fieldsStats = fieldsStatsByQuery.get(topicId);
	    		JsonNode uCollFreq = uCollFreqByQuery.get(topicId);
	    		JsonNode bCollFreq = bCollFreqByQuery.get(topicId);
	    		
				for(Document doc : docsByQuery.get(topicId)){
					newRanking.put(doc.getUrl(), fsdm.finalScore(unigrams, bigrams, doc, fieldsStats, uCollFreq, bCollFreq));
				}
				
				HashMap<String, Double> orderedFSDMRanking = (HashMap<String, Double>) MapUtil.sortByValue(newRanking);
				int c = 0;
				for (Entry<String, Double> result : orderedFSDMRanking.entrySet()) {
					System.out.println(result.getKey() + " " + result.getValue());
					if (c == 10 )
						break;
					c++;
				}
	    		break;
	    		
	        	
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		

	}

}
