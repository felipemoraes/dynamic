package br.ufmg.dcc.latin.metrics;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class CubeTest {
	
	private int MAX_HEIGHT = 5;
	private double gamma = 0.5f;
	
	// $topic $docno $subtopic $judgement
	private Map<String, Map<String, Map<String, Double> > >  qrels;
	// $topic $subtopic $area
	private Map<String, Map<String, Double > >  subtopicWeight;
	// subtopic gain
	private Map<String, Double> currentGainHeight;
	// subtopic cover
	private Map<String, Integer> subtopicCover;
	
	public CubeTest(){
		qrels = new HashMap<String, Map<String, Map<String, Double> > > ();
		subtopicWeight = new HashMap<String, Map<String, Double > >();

		Map<String, Map<String, Map<String, List<Integer>> > >   tmpQrels = new HashMap<String, Map<String, Map<String, List<Integer> > > > ();
		try (BufferedReader br = new BufferedReader(new FileReader("../share/dd_qrels.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitedLine = line.split("\t",5);
				
				String topic = splitedLine[0];
				String subtopic = splitedLine[1];
				String docno = splitedLine[2];
				
				int judgment = Integer.parseInt(splitedLine[4]);
				if (judgment == 0) {
					judgment = 1;
				}
				if (!tmpQrels.containsKey(topic)){
					subtopicWeight.put(topic, new HashMap<String,Double>());
					tmpQrels.put(topic, new HashMap<String, Map<String, List<Integer> > >());
				} 
				
				if (!tmpQrels.get(topic).containsKey(docno)){
					
					tmpQrels.get(topic).put(docno, new HashMap<String, List<Integer> > ());
				}
				
				if (!tmpQrels.get(topic).get(docno).containsKey(subtopic)){
					subtopicWeight.get(topic).put(subtopic, 1d);
					tmpQrels.get(topic).get(docno).put(subtopic, new ArrayList<Integer>());
				}
				
				tmpQrels.get(topic).get(docno).get(subtopic).add(judgment);
				
			}
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		for (Entry<String, Map<String, Map<String, List<Integer> > >> entryTopic : tmpQrels.entrySet()) {
			
			
			String topic = entryTopic.getKey();
			
			if (!qrels.containsKey(entryTopic.getKey())){
				qrels.put(entryTopic.getKey(), new HashMap<String, Map<String, Double > >());
			} 
			
			for (Entry<String, Map<String, List<Integer>>> entryDocno: entryTopic.getValue().entrySet()){
				String docno = entryDocno.getKey();
				if (!qrels.get(topic).containsKey(docno)){
					qrels.get(topic).put(docno, new HashMap<String, Double >());
				}
				
				for(Entry<String, List<Integer>> entrySubtopic: entryDocno.getValue().entrySet()){
					
					String subtopic = entrySubtopic.getKey();	
					Collections.sort(entrySubtopic.getValue());
					Collections.reverse(entrySubtopic.getValue()); 
					List<Integer> rels = entrySubtopic.getValue();
					double rel = 0;
					double log2 = Math.log(2);
					
					for (int i = 0; i < rels.size(); i++) {
						rel += rels.get(i)/(Math.log(i+2)/log2);;
					}
					
					qrels.get(topic).get(docno).put(subtopic, rel);
				}
				
			}
			
		}
		
		// Normalize subtopic weight
		for(Entry<String, Map<String,Double>> entry: subtopicWeight.entrySet()){
			double maxWeight = getMaxWeight(entry.getKey());
			for (Entry<String,Double> subtopic: entry.getValue().entrySet()) {
				subtopic.setValue(subtopic.getValue()/maxWeight);
			}
		}
	}
	
	private double getMaxWeight(String topic) {
		double maxWeight = 0;
		for (Entry<String,Double> subtopic: subtopicWeight.get(topic).entrySet()) {
			maxWeight += subtopic.getValue();
		}
		return maxWeight;
	}

	public double getCubeTest(int iteration, String topic, String[][] docnos){
		currentGainHeight = new HashMap<String, Double>();
		subtopicCover = new HashMap<String, Integer>();
		double score = 0;
		int maxIterations = 0;
		for (int i = 0; i < iteration; i++) {
			if (docnos[i] == null) {
				break;
			}
			maxIterations++;
			for (int j = 0; j < docnos[i].length; j++) {
				if (docnos[i][j] == null) {
					continue;
				}
				double gain = getDocGain(topic,docnos[i][j]);
				score += gain;
			}
			
		}
		maxIterations = Math.min(maxIterations, iteration);
		double ct = score/MAX_HEIGHT;
		double ctSpeed = ct/(maxIterations);
		return ctSpeed;
	}
	
	private double getDocGain(String topic, String docno){
		double docGain = 0;
		for (Entry<String,Double> subtopicEntry : subtopicWeight.get(topic).entrySet()) {
			String subtopic = subtopicEntry.getKey();
			
			if (!currentGainHeight.containsKey(subtopic)){
				currentGainHeight.put(subtopic, 0d);
				subtopicCover.put(subtopic, 0);
				
			}
			
			if (qrels.get(topic).containsKey(docno)){
				if (!qrels.get(topic).get(docno).containsKey(subtopic)){
					continue;
				}

			} else {
				continue;
			}
			
			double area = subtopicEntry.getValue();
			int nrel = subtopicCover.get(subtopic);
			double hightKeepfilling = getHightKeepFilling(topic,docno,subtopic,nrel+1);
			docGain += area*hightKeepfilling;
			
			subtopicCover.put(subtopic, nrel+1);
		}
		return docGain;
	}
	
	private double getHightKeepFilling(String topic, String docno, String subtopic, int nrel){
		double rel = 0;
		
		if (qrels.get(topic).containsKey(docno)) {
			rel = qrels.get(topic).get(docno).getOrDefault(subtopic, 0d);
			if (rel == 0){
				return 0;
			}
		}
		
		double currentGain = currentGainHeight.get(subtopic);
		
		double gain = getHightDiscount(nrel)*rel;
		
		if (currentGain + gain > MAX_HEIGHT){
			gain = MAX_HEIGHT - currentGain;
		}
		
		currentGainHeight.put(subtopic, currentGain+gain);
		return gain;
	
	}
	
	private double getHightDiscount(int nrel) {
		return Math.pow(gamma, nrel);
	}

	public double getAverageCubeTest(int iteration, String topic, String[][] docnos){
		
		currentGainHeight = new HashMap<String, Double>();
		subtopicCover = new HashMap<String, Integer>();
		double score = 0;
		double ctAccu = 0;
		int time = 0;
		for (int i = 0; i < iteration; i++) {
			if (i > docnos.length){
				break;
			}
			if (docnos[i] == null) {
				for (int j = 0; j < 5; j++) {
					//double act = score/MAX_HEIGHT;
					//System.out.println(docnos[i][j]);
					//ctAccu += act/(i+1);
					//time++;
				}
				continue;
			}
			for (int j = 0; j < docnos[i].length; j++) {
				if (docnos[i][j] == null){
					//double act = score/MAX_HEIGHT;
					//System.out.println(docnos[i][j]);
					//ctAccu += act/(i+1);
					//time++;
					continue;
				}
				double gain = getDocGain(topic,docnos[i][j]);
				score += gain;
				double act = score/MAX_HEIGHT;
				//System.out.println(docnos[i][j]);
				ctAccu += act/(i+1);
				time++;
			}
			
		}
		//System.out.println();
		return ctAccu/time;
	}
	
	public double getGain(int iteration, String topic, String[][] docnos){
		
		currentGainHeight = new HashMap<String, Double>();
		subtopicCover = new HashMap<String, Integer>();
		double score = 0;
		double ctAccu = 0;
		int time = 0;
		for (int i = 0; i < iteration; i++) {
			if (i > docnos.length){
				break;
			}
			if (docnos[i] == null) {
				break;
			}
			for (int j = 0; j < docnos[i].length; j++) {
				if (docnos[i][j] == null){
					continue;
				}
				double gain = getDocGain(topic,docnos[i][j]);
				score += gain;
				double act = score/MAX_HEIGHT;
				//System.out.println(docnos[i][j]);
				ctAccu += act/(i+1);
				time++;
			}
			
		}
		//System.out.println();
		return score;
	}
}