package br.ufmg.dcc.latin.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Main {

	public static void main(String[] args) throws IOException {
		String topicsFile = "../share/topics_domain.txt";
		String type = args[0];
		
		BufferedReader br = new BufferedReader(new FileReader(topicsFile));
	    String line;
	    Session session = null;

	    
	    
	    TrecUser.load("../share/truth_data_deduped.txt");
	    
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",3);
	    	
        	String topicId = splitLine[1];
        	System.out.println(topicId);
        	//if (!topicId.equals("DD16-1")){
        	//	continue;
        	//}
    		
    		String query = splitLine[2].replaceAll("/", " ");
    		session.start(splitLine[0], topicId, query);
    		session.run();
    		
	    }
		br.close();
	}

}
