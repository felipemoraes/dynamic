package br.ufmg.dcc.latin.simulator.dd;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.ufmg.dcc.latin.searcher.utils.DDFeedback;
import br.ufmg.dcc.latin.searcher.utils.DDFeedbackSignals;
import br.ufmg.dcc.latin.searcher.utils.FeedbackSignals;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;
import br.ufmg.dcc.latin.searcher.utils.Subtopic;
import br.ufmg.dcc.latin.simulator.Simulator;

public class DDSimulator implements Simulator {

	private HashMap<String, HashMap<String, List<Subtopic>> >  truthData; 
	
	private HashMap< String, HashMap<String,Integer >> runs;
	
	public DDSimulator(String topicFilename) throws FileNotFoundException, IOException{
			truthData = new HashMap<String, HashMap<String, List<Subtopic> > >();
			try (BufferedReader br = new BufferedReader(new FileReader(topicFilename))) {
				String line;
    		    while ((line = br.readLine()) != null) {
    		    	String[] splitLine = line.split(",",5);
    		    	Subtopic subtopic = new Subtopic(splitLine[2], Integer.parseInt(splitLine[3]), splitLine[4]);
    		    	
    		    	if (!truthData.containsKey(splitLine[0])){
    		    		truthData.put(splitLine[0], new HashMap<String, List<Subtopic>>());
    		    		if (!truthData.get(splitLine[0]).containsKey(splitLine[1])){
    		    			truthData.get(splitLine[0]).put(splitLine[1], new ArrayList<Subtopic>());
    		    		}
    		    	} else  if (!truthData.get(splitLine[0]).containsKey(splitLine[1])){
    		    		truthData.get(splitLine[0]).put(splitLine[1], new ArrayList<Subtopic>());
    		    	}
    		    	
    		    	truthData.get(splitLine[0]).get(splitLine[1]).add(subtopic);
    		    }	
			}
			runs = new HashMap< String, HashMap<String,Integer >>();
			
	}

	public DDSimulator(){
		
	}
	
	public FeedbackSignals performStepWithJig(String runId, String topicId, ResultSet resultSet) {
	
   		String cmd = "python";
		cmd += " src/main/resources/trecdd/jig/jig.py";
		cmd += " -runid " + runId + " -topic " + topicId +" -docs " + resultSet.toString();
		
        Process p;
        DDFeedbackSignals ddFeedbackSignals;
        System.out.println(cmd);
		try {
			p = Runtime.getRuntime().exec(cmd);
	        BufferedReader stdInput = new BufferedReader(new
	                InputStreamReader(p.getInputStream()));
	        String feedback = "";
	        String s = null;
	        while ((s = stdInput.readLine()) != null) {
	        	feedback += s;
	        }
	        System.out.println(feedback);
	        ddFeedbackSignals = new DDFeedbackSignals(feedback);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return ddFeedbackSignals;
	}
	
	
	
	@Override
	public FeedbackSignals performStep(String runId, String topicId, ResultSet resultSet) {
		// Get 
		
		if (!runs.containsKey(runId)) {
			runs.put(runId, new HashMap<String,Integer>());
		}
		Integer ct = runs.get(runId).getOrDefault(topicId, 0);
		DDFeedbackSignals feedbackSignals = new DDFeedbackSignals();
		try(FileWriter fw = new FileWriter(runId + ".txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw))
		{
			for (Entry<String, Double> result: resultSet.getResultSet().entrySet()) {
				DDFeedback dDFeedback = new DDFeedback();
				dDFeedback.setDocId(result.getKey());
				dDFeedback.setRankingScore(result.getValue());

				
				String wline = topicId + "\t" + ct.toString() +"\t" 
						+ result.getKey() + "\t" + result.getValue().toString() + "\t";
				
				if (!truthData.containsKey(result.getKey())){
					dDFeedback.setOnTopic("0");
					wline += "0";
				} else if (truthData.get(result.getKey()).containsKey(topicId)) {
					wline += "1" +  "\t";
					for (Subtopic subtopic : truthData.get(result.getKey()).get(topicId)) {
						wline += subtopic.getSubtopicId() + ":" + subtopic.getRating().toString() + "|";
						
					}
					wline = wline.substring(0,wline.length()-1);
					dDFeedback.setOnTopic("1");
				} else {
					dDFeedback.setOnTopic("0");
					wline += "0";
				}
				feedbackSignals.addDdFeedback(dDFeedback);
				out.println(wline);
			}
		
		} catch (IOException e) {
			
		}
		ct++;
		runs.get(runId).put(topicId, ct);
		
		return feedbackSignals;
	}
	
	

}
