package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;

import br.ufmg.dcc.latin.controlers.DynamicSearchController;
import br.ufmg.dcc.latin.controlers.SearchResponse;
import br.ufmg.dcc.latin.search.elements.Feedback;



/**
 * @author Felipe Moraes
 */

public class Main {

    public static void main(String[] args) throws FileNotFoundException, IOException {
    	
    	String runId = "xQuADi_03";
   
    	
    	String topicsFile = "src/main/resources/topics_domain_2016.txt";
		DynamicSearchController controller = new DynamicSearchController();
		
		DDSimulator simulator = new DDSimulator("src/main/resources/truth_data_2016.txt");
    
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
	        	//if (!topicId.equals("DD16-8")){
	        	//	continue;
	        	//}
	        	
	    		String query = StringEscapeUtils.escapeJava(splitLine[2]);
	    		query = query.replaceAll("/", " ");
	    
	    		System.out.println(topicId);
	    		int token = controller.initSearch(indexName, "xQuADi", query);
	    		//String docNos[] = controller.getDocNos(token);
	    		//controller.setCoverage(simulator.getCoverage(topicId, docNos), token);
	    		//controller.setImportance(simulator.getImportance(topicId),token);
	    		SearchResponse response = controller.searchQuery(indexName, "xQuADi", query, token);
	        	for (int i = 0; i < 20; i++) {
	        		Feedback[] feedback = simulator.performStep(runId, topicId, response.getResponse());
	        		controller.updateFeedback(feedback, response.getToken());
	        		response = controller.searchQuery(indexName, "xQuADi", query, response.getToken());
	        		break;
				}
	        	break;
	        	
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    }    
    
}
