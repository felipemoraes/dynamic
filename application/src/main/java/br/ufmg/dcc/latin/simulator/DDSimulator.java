package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import br.ufmg.dcc.latin.search.elements.Feedback;
import br.ufmg.dcc.latin.search.elements.Subtopic;


public class DDSimulator {

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


	/*
	public FeedbackSignals performStepWithJig(String runId, String topicId, FeaturedResultSet resultSet) {
	
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
	
	*/
	


	LinkedHashMap<String,Double> sortByValue(Map<String, Double> result){
		LinkedHashMap<String,Double> sortedResult = new LinkedHashMap<String,Double>();
		for (Entry<String,Double> entry1: result.entrySet()){
			Double maxValue = 0.0;
			String max = entry1.getKey();
			for (Entry<String,Double> entry2: result.entrySet()){
				if (entry2.getValue() > maxValue){
					if (sortedResult.containsKey(entry2.getKey())){
						continue;
					}
					
					max = entry2.getKey();
					maxValue = entry2.getValue();
				}
			}
			
			sortedResult.put(max, result.get(max));
		}
		return sortedResult;
	}
	
	public double[][][] getCoverage(String topicId, String[] docIds){
		
		
		HashMap<String,Integer> subtopics = new HashMap<String,Integer>();
		HashMap<String,HashMap<String,Integer>> passages = new HashMap<String,HashMap<String,Integer>>();
		int si = 0;
		for (Entry<String,HashMap<String, List<Subtopic> >> entry: truthData.entrySet()){
			if (entry.getValue().containsKey(topicId)){
				for (Subtopic subtopic : entry.getValue().get(topicId)) {
					if (!subtopics.containsKey(subtopic.getId())) {
						subtopics.put(subtopic.getId(), si);
						si++;
					}
					if (!passages.containsKey(subtopic.getId())){
						passages.put(subtopic.getId(), new HashMap<String,Integer>());
						
					}
					int ix = passages.get(subtopic.getId()).size();
					if (!passages.get(subtopic.getId()).containsKey(subtopic.getPassageText())){
						passages.get(subtopic.getId()).put(subtopic.getPassageText(),ix);
						ix++;
					}
					
				}
			}
		}
	
		int k = subtopics.size();
		
		int n = docIds.length;
		
		double[][][] coverage = new double[n][k][];
		
		for (String subtopic : subtopics.keySet()){
			int j = subtopics.get(subtopic);
			System.out.println("Subtopic " + j + " has " + passages.get(subtopic).size() + " passages"  );
			for (int i = 0; i < coverage.length; i++) {
				coverage[i][j] = new double[passages.get(subtopic).size()];
				Arrays.fill(coverage[i][j], 0);
			}
		}
	
		for (int i = 0; i < docIds.length; ++i){
			if (truthData.containsKey(docIds[i])){
				if (truthData.get(docIds[i]).containsKey(topicId)) {
					for (Subtopic subtopic : truthData.get(docIds[i]).get(topicId)) {
						int j = subtopics.get(subtopic.getId());
						int l = passages.get(subtopic.getId()).get(subtopic.getPassageText());
						
						coverage[i][j][l] = (double) (subtopic.getRating()*2)/10;
					}
				}
			}
		}
		
		
		
		return coverage;
	}
	
	public double[] getImportance(String topicId){
		
		HashMap<String,Integer> subtopics = new HashMap<String,Integer>();
		HashMap<String,Integer> subtopicsSize = new HashMap<String,Integer>();
		int ix = 0;
		

		for (Entry<String,HashMap<String, List<Subtopic> >> entry: truthData.entrySet()){
			if (entry.getValue().containsKey(topicId)){
				for (Subtopic subtopic : entry.getValue().get(topicId)) {
					if (!subtopics.containsKey(subtopic.getId())) {
						subtopics.put(subtopic.getId(),ix);
						subtopicsSize.put(subtopic.getId(), 0);
						ix++;
					}
			
					subtopicsSize.put(subtopic.getId(), subtopicsSize.get(subtopic.getId())+1);
				}
			}
		}
	
		int k = subtopics.size();
		int sum = 0;
		for(Integer value : subtopicsSize.values()){
			sum += value;
		}
		
		double[] importance = new double[k];
		for (Entry<String,Integer> subtopic: subtopics.entrySet()){
			int i = subtopic.getValue();
		//	importance[i] = (double) subtopicsSize.get(subtopic.getKey())/sum;
			importance[i] = 1;
		//	System.out.println(subtopic.getKey() + " " + subtopicsSize.get(subtopic.getKey()));
		}

		return importance;
	}
	
	
	public Feedback[] getAllFeedback(String topicId){
		
		ArrayList<Feedback> accFeedback = new ArrayList<Feedback>();
		for (Entry<String,HashMap<String, List<Subtopic> >> entry: truthData.entrySet()){
			if (entry.getValue().containsKey(topicId)){
				Feedback feedback = new Feedback();
				feedback.setOnTopic(true);
				
				int k =  truthData.get(entry.getKey()).get(topicId).size();
				Subtopic[] subtopics = new Subtopic[k];
				int i = 0;
				for (Subtopic subtopic : entry.getValue().get(topicId)) {
					subtopics[i] = subtopic;
					i++;
				}
				feedback.setSubtopics(subtopics);
				accFeedback.add(feedback);
			}
		}
		Feedback[] feedback = new Feedback[accFeedback.size()];
		int i = 0;
		for (Feedback f : accFeedback) {
			feedback[i] = new Feedback();
			feedback[i].setDocId(f.getDocId());
			feedback[i].setOnTopic(f.getOnTopic());
			feedback[i].setScore(f.getScore());
			feedback[i].setSubtopics(f.getSubtopics());
			i++;
		}
		return feedback;
		
	}
	
 	public Feedback[] performStep(String runId, String topicId, Map<String, Double> resultSet) {
		// Get 
		
		if (!runs.containsKey(runId)) {
			runs.put(runId, new HashMap<String,Integer>());
		}
		

		
		
		
		Integer ct = runs.get(runId).getOrDefault(topicId, 0);
		ArrayList<Feedback> accFeedback = new ArrayList<Feedback>();
		int p = 0;
		try(FileWriter fw = new FileWriter(runId + ".txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw))
		{
			for (Entry<String, Double> result: sortByValue(resultSet).entrySet()) {
				Feedback feedback = new Feedback();
				feedback.setDocId(result.getKey());
				feedback.setScore(result.getValue());
				
				double score = (5000.0-ct-p)/5000.0;
				p++;
				//String wline = topicId + "\t" + ct.toString() +"\t" 
				//		+ result.getKey() + "\t" + String.format("%.12f", result.getValue()) + "\t";
				
				String wline = topicId + "\t" + ct.toString() +"\t" 
						+ result.getKey() + "\t" + String.format("%.12f", score) + "\t";
				if (!truthData.containsKey(result.getKey())){
					feedback.setOnTopic(false);
					wline += "0\tNULL";
				} else if (truthData.get(result.getKey()).containsKey(topicId)) {
					wline += "1" +  "\t";
					int k =  truthData.get(result.getKey()).get(topicId).size();
					Subtopic[] subtopics = new Subtopic[k];
					int i = 0;
					for (Subtopic subtopic : truthData.get(result.getKey()).get(topicId)) {
						wline += subtopic.getId() + ":" + subtopic.getRating().toString() + "|";
						subtopics[i] = subtopic;
						i++;
					}
					wline = wline.substring(0,wline.length()-1);
					feedback.setOnTopic(true);
					feedback.setSubtopics(subtopics);
				} else {
					feedback.setOnTopic(false);
					wline += "0\tNULL";
				}
				accFeedback.add(feedback);
				out.println(wline);
				
			}
		
		} catch (IOException e) {
			
		}
		ct++;
		runs.get(runId).put(topicId, ct);
		Feedback[] feedback = new Feedback[accFeedback.size()];
		int i = 0;
		for (Feedback f : accFeedback) {
			feedback[i] = new Feedback();
			feedback[i].setDocId(f.getDocId());
			feedback[i].setOnTopic(f.getOnTopic());
			feedback[i].setScore(f.getScore());
			feedback[i].setSubtopics(f.getSubtopics());
			i++;
		}
		
		return feedback;
	}
	
	

}
