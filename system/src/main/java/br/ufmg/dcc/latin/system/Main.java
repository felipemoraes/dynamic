package br.ufmg.dcc.latin.system;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import br.ufmg.dcc.latin.system.session.LMSession;
import br.ufmg.dcc.latin.system.session.MMRSession;
import br.ufmg.dcc.latin.system.session.PM2Session;
import br.ufmg.dcc.latin.system.session.xQuADSession;

public class Main {

	public static void main(String[] args) throws IOException {
		String topicsFile = "../share/topics_domain.txt";
		String type = args[0];
		
		BufferedReader br = new BufferedReader(new FileReader(topicsFile));
	    String line;
	    Session session = null;
	    
	    if (type.equals("xQuAD")){
	    	session = new xQuADSession();
	    } else if (type.equals( "PM2")) {
	    	session = new PM2Session();
	    }  else if (type.equals("MMR")) {
	    	session = new MMRSession();
	    }  else if (type.equals( "LM")) {
	    	session = new LMSession();
	    } 
	    
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
