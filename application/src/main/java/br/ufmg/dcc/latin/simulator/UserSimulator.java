package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.util.Scanner;

import br.ufmg.dcc.latin.searcher.AdHocSearcherFactory;
import br.ufmg.dcc.latin.searcher.Searcher;
import br.ufmg.dcc.latin.searcher.similarity.LMDirichletSimilarity;

import org.apache.log4j.Logger;

/**
 * @author Felipe Moraes
 */

public class UserSimulator {

    private static final Logger LOGGER = Logger.getLogger(UserSimulator.class);
	

    public static void main(String[] args) throws UnknownHostException {
    	
    	String runId = "testrun9";
    	String topicsFile = "src/main/resources/topics_domain.txt";
    	
    		//String s = null;

    		
		LMDirichletSimilarity lmSimilarity = new LMDirichletSimilarity(1800.0);
		AdHocSearcherFactory adHocSearcherFactory = new AdHocSearcherFactory(lmSimilarity);
		
		
    		
    
    		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
    		    String line;
    		    while ((line = br.readLine()) != null) {

    		    	String[] splitLine = line.split(" ",3);
    	        	String indexName = splitLine[0];
    	        	if (indexName.equals("Ebola")) {
    	        		indexName = "ebola_2015";
    	        		continue;
					} else if (indexName.equals("Local_Politics")){
						indexName = "local_politics";
						continue;
					} else if (indexName.equals("Illicit_Goods")){
						indexName = "illicit_goods";
					}
    	        	
    	        	String topicId = splitLine[1];
    	    		String query = splitLine[2];
    	    		
    	    		System.out.println(topicId);
		        
		        	Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(indexName,lmSimilarity);
		        	adHocSearcher.search(query);
		        	for (int i = 0; i < 20; i++) {
		        		String docs = adHocSearcher.getNextResults();
		        		if (docs == null) {
		        			break;
		        		}
	    	       		String cmd = "python";
	            		cmd += " src/main/resources/trec-dd-jig/jig/jig.py";
			    		cmd += " -runid " + runId + " -topic " + topicId +" -docs " + docs;
			    		
			            Process p;
						
						p = Runtime.getRuntime().exec(cmd);
			
			             
			            BufferedReader stdInput = new BufferedReader(new
			                 InputStreamReader(p.getInputStream()));
			            // read the output from the command
			            
			            String feedback = "";
			            String s = null;
			            while ((s = stdInput.readLine()) != null) {
			            	feedback += s;
			            }
			            
			            System.out.println(feedback);
			            //Scanner scanner = new Scanner(System.in);
			            //String pass = scanner.next();
					}
				    
		            
    		    }
    		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
              
 
        
    
}
