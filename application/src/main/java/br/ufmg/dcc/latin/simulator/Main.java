package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.lucene.queryparser.classic.ParseException;

/**
 * @author Felipe Moraes
 */

public class Main {


	
    public static void main(String[] args) throws ParseException, IOException { 	
    	
    	String topicsFile = "src/main/resources/topics_domain_2016.txt";
		
		
		String type = args[0];
		
		String runId = "data/" + args[0] + "_" + args[1] + "_" + args[2] ;
		//String runId = "data/" + args[0] + "_SS3_15";
		DDServicer servicer = new DDServicer(runId);
		BufferedReader br = new BufferedReader(new FileReader(topicsFile));
	    String line;
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",3);
        	String indexName = getIndexName(splitLine[0]);
        	
        	String topicId = splitLine[1];
        	System.out.println(topicId);
        	//if (!topicId.equals("DD16-47")){
        	//	continue;
        	//}
    		String query = StringEscapeUtils.escapeJava(splitLine[2]);
    		query = query.replaceAll("/", " ");
    		
    		servicer.run(type,indexName,query,topicId,args);
    		
	    }
		br.close();
		
    }    
    
    
    private static String getIndexName(String domain){
    	String indexName = null;
    	if (domain.equals("Ebola")) {
    		indexName = "ebola_2016";
		} else if (domain.equals("Polar")){
			indexName = "polar_2016";
		}
    	
    	return indexName;
    	
    }
    
   
    
}
