package br.ufmg.dcc.latin.simulator.dd;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import br.ufmg.dcc.latin.searcher.utils.DDFeedback;
import br.ufmg.dcc.latin.searcher.utils.DDFeedbackSignals;
import br.ufmg.dcc.latin.searcher.utils.DocumentTopicPair;
import br.ufmg.dcc.latin.searcher.utils.FeedbackSignals;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;
import br.ufmg.dcc.latin.searcher.utils.Subtopic;
import br.ufmg.dcc.latin.simulator.Simulator;

public class DDSimulator implements Simulator {

	private HashMap<DocumentTopicPair, List<Subtopic>> truthData; 
	
	private HashMap<RunTopicPair,Integer> runs;
	
	public DDSimulator(String topicFilename) throws FileNotFoundException, IOException{
			truthData = new HashMap<DocumentTopicPair, List<Subtopic>>();
			try (BufferedReader br = new BufferedReader(new FileReader(topicFilename))) {
				String line;
    		    while ((line = br.readLine()) != null) {
    		    	String[] splitLine = line.split(" ",4);
    		    	DocumentTopicPair documentTopicPair = new DocumentTopicPair(splitLine[0],splitLine[1]);
    		    	Subtopic subtopic = new Subtopic(splitLine[2], Integer.parseInt(splitLine[3]), splitLine[4]);
    		    	
    		    	if (truthData.containsKey(documentTopicPair)){
    		    		truthData.get(documentTopicPair).add(subtopic);
    		    	} else {
    		    		List<Subtopic> subtopics = new ArrayList<Subtopic>();
    		    		subtopics.add(subtopic);
    		    		truthData.put(documentTopicPair, subtopics);
    		    	}
    		    }	
			}
			runs = new HashMap<RunTopicPair,Integer>();
			
		// initiate set <DDRun>
	}
	
	@Override
	public FeedbackSignals performStep(String runId, String topicId, ResultSet resultSet) {
		// Get 
		RunTopicPair runTopicPair = new RunTopicPair(runId, topicId);
		Integer ct = runs.getOrDefault(runTopicPair, 0);
		
		DDFeedbackSignals feedbackSignals = new DDFeedbackSignals();
		for (Entry<String, Double> result: resultSet.getResultSet().entrySet()) {
			DDFeedback dDFeedback = new DDFeedback();
			dDFeedback.setDocId(result.getKey());
			dDFeedback.setRankingScore(result.getValue());
			DocumentTopicPair documentTopicPair = new DocumentTopicPair(result.getKey(), topicId);
			List<Subtopic> subtopics = truthData.getOrDefault(documentTopicPair, new ArrayList<Subtopic>());
			if (subtopics.isEmpty()){
				dDFeedback.setOnTopic("0");
			} else {
				dDFeedback.setOnTopic("1");
			}
			feedbackSignals.addDdFeedback(dDFeedback);
			//TODO: write in runfile
		}
		ct++;
		runs.put(runTopicPair, ct);
		
		return feedbackSignals;
	}
	
	

}
