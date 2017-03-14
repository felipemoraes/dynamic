package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.cache.RetrievalCache;

public class DynamicSystem {
	
	private static DynamicSystemParameters parameters;
	
	public static void main(String[] args) throws IOException {
		String topicsFile = "../share/topics_domain.txt";
		//parameters = ParametersController.getParameters(args[0]);
		
		BufferedReader br = new BufferedReader(new FileReader(topicsFile));
	    String line;
	   
	    Session session = new Session();
	    parameters = new DynamicSystemParameters();
	    parameters.reranker = "HxQuAD";
	    double[] param = {100f, 10f, 0.5f};
	    List<double[]> listParams = new ArrayList<double[]>();
	    listParams.add(param);
	    parameters.experimentalParameters = listParams;
	    session.setReranker(parameters.reranker);
	    session.setParams(parameters.experimentalParameters);
	    
	    TrecUser.load("../share/truth_data.txt");
	    
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",3);
	    	
        	String topicId = splitLine[1];
        	RetrievalCache.topicId = topicId;
        	if (!topicId.equals("DD16-1")){
        		continue;
        	}
        	System.out.println(topicId);
    		
    		String query = splitLine[2].replaceAll("/", " ");
    		session.run(splitLine[0], topicId, query);
    		
	    }
		br.close();
	}

}
