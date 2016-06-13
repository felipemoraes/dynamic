package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import br.ufmg.dcc.latin.searcher.AdHocSearcherFactory;
import br.ufmg.dcc.latin.searcher.Searcher;
import br.ufmg.dcc.latin.searcher.models.LMDirichlet;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;
import br.ufmg.dcc.latin.simulator.dd.DDSimulator;

/**
 * @author Felipe Moraes
 */

public class UserSimulator {

    public static void main(String[] args) throws FileNotFoundException, IOException {
    	
    	String runId = "testrun9";
    	String topicsFile = "src/main/resources/sample_topics_domain.txt";

    		
		LMDirichlet lmSimilarity = new LMDirichlet(1800.0);
		AdHocSearcherFactory adHocSearcherFactory = new AdHocSearcherFactory(lmSimilarity);
		
		DDSimulator simulator = new DDSimulator("src/main/resources/truth_data.txt");
		//DDSimulator simulator = new DDSimulator();
    
		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {

		    	String[] splitLine = line.split(" ",3);
	        	String indexName = splitLine[0];
	        	if (indexName.equals("Ebola")) {
	        		indexName = "ebola_2015";
				} else if (indexName.equals("Local_Politics")){
					indexName = "local_politics";
				} else if (indexName.equals("Illicit_Goods")){
					indexName = "illicit_goods";
				}
	        	
	        	String topicId = splitLine[1];
	    		String query = splitLine[2];
	    		
	    		System.out.println(topicId);
	        
	        	Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(indexName);
	        	adHocSearcher.search(query);
	        	for (int i = 0; i < 2; i++) {
	        		ResultSet resultSet = adHocSearcher.getNextResults();
	        		simulator.performStep(runId, topicId, resultSet);
				}
		    }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }    
    
}
