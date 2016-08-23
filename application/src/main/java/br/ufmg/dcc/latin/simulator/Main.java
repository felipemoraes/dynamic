package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryparser.classic.ParseException;

import br.ufmg.dcc.latin.models.MMR;
import br.ufmg.dcc.latin.models.xQuADiClassifier;
import br.ufmg.dcc.latin.models.xQuADiLDA;
import br.ufmg.dcc.latin.models.xQuADiOracle;
import br.ufmg.dcc.latin.models.xQuADiTFIDF;
import br.ufmg.dcc.latin.search.elements.Feedback;
import br.ufmg.dcc.latin.searcher.WeightingModule;
import br.ufmg.dcc.latin.searcher.es.models.LMDirichlet;



/**
 * @author Felipe Moraes
 */

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
    	
    	String runId = "baseline_mmr";
    	
    	
    	
    	
    	
    	String topicsFile = "src/main/resources/topics_domain_2016.txt";
    	//xQuADiTFIDF xQuADi = new xQuADiTFIDF();
    	MMR mmr = new MMR();
    	
		
		DDSimulator simulator = new DDSimulator("src/main/resources/truth_data_2016.txt");
		
		//WeightingModule.changeWeightingModel("ebola_2016", new LMDirichlet(2500.0));
		//WeightingModule.changeWeightingModel("polar_2016", new LMDirichlet(2500.0));
		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {

		    	String[] splitLine = line.split(" ",3);
	        	String indexName = splitLine[0];
	        	if (indexName.equals("Ebola")) {
	        		indexName = "ebola_2016";
				} else if (indexName.equals("Polar")){
					indexName = "polar_2016";
				}
	        	
	        	String topicId = splitLine[1];
	        	//if (!topicId.equals("DD16-18")){
	        	//	continue;
	        	//}
	        	
	    		String query = StringEscapeUtils.escapeJava(splitLine[2]);
	    		query = query.replaceAll("/", " ");
	    
	    		System.out.println(topicId);
	    		
	    		
	    		mmr.create(indexName, query);
	    		
	    		//xQuADi.coverage = simulator.getCoverage(topicId, xQuADi.docNos);
	    		//xQuADi.importance = simulator.getImportance(topicId);
	    		//xQuADi.k = xQuADi.importance.length;
	    		
	    		//controller.setCoverage(simulator.getCoverage(topicId, docNos), token);
	    		//controller.setImportance(simulator.getImportance(topicId),token);
	    		Map<String,Double> response = mmr.get();
	        	for (int i = 0; i < 20; i++) {
	        		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	        		//xQuADi.update(feedback);
	        		response = mmr.get();
	        		
				}
	    		
	        	
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
    }    
    
}
