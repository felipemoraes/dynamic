package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DynamicSystem {
	
	private static DynamicSystemParameters parameters;
	
	public static void main(String[] args) throws IOException {
		String topicsFile = "../share/topics_domain.txt";
		parameters = ParametersController.getParameters(args[0]);
		
		BufferedReader br = new BufferedReader(new FileReader(topicsFile));
	    String line;
	   
	    Session session = new Session();
	    /*parameters = new DynamicSystemParameters();
	    parameters.reranker = "MMR";
	    float[] param = {1000f, 0.2f};
	    List<float[]> listParams = new ArrayList<float[]>();
	    listParams.add(param);
	    parameters.experimentalParameters = listParams;*/
	    session.setReranker(parameters.reranker);
	    session.setParams(parameters.experimentalParameters);

	    TrecUser.load("../share/truth_data_deduped.txt");
	    
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",3);
	    	
        	String topicId = splitLine[1];
        	
        	//if (!topicId.equals("DD16-1")){
        	//	continue;
        	//}
        	System.out.println(topicId);
    		
    		String query = splitLine[2].replaceAll("/", " ");
    		session.run(splitLine[0], topicId, query);
    		
	    }
		br.close();
	}

}
