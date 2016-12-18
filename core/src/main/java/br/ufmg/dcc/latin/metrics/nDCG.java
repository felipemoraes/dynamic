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

public class nDCG {
	

	
	// $topic $docno $judgement
	private Map<String, Map<String, Integer> >   qrels;
	// $topic $idcg
	private Map<String, Double > IDCG;

	
	public nDCG(){
		qrels = new HashMap<String, Map<String, Integer> > ();
		IDCG = new HashMap<String, Double >();

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
					tmpQrels.put(topic, new HashMap<String, Map<String, List<Integer> > >());
				} 
				
				if (!tmpQrels.get(topic).containsKey(docno)){
					
					tmpQrels.get(topic).put(docno, new HashMap<String, List<Integer> > ());
				}
				
				if (!tmpQrels.get(topic).get(docno).containsKey(subtopic)){
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
				qrels.put(entryTopic.getKey(), new HashMap<String, Integer >());
			} 
			
			for (Entry<String, Map<String, List<Integer>>> entryDocno: entryTopic.getValue().entrySet()){
				String docno = entryDocno.getKey();				
				List<Integer> allRels = new ArrayList<Integer>();
				for(Entry<String, List<Integer>> entrySubtopic: entryDocno.getValue().entrySet()){

					List<Integer> rels = entrySubtopic.getValue();
					
					allRels.addAll(rels);
					
				}
				float sum = 0;
				for (int i = 0; i < allRels.size(); i++) {
					if (allRels.get(i) == 0 ) {
						sum+=1;
					} else {
						sum += allRels.get(i);
					}
				}
				int rel = (int) Math.floor(sum/allRels.size());

				qrels.get(topic).put(docno, rel);
				
			}
			
			List<Integer> rels = new ArrayList<Integer>();
			

			rels.addAll(qrels.get(topic).values());
			Collections.sort(rels);
			Collections.reverse(rels);
			double idcg = 0;
			
			for (int i = 0; i < rels.size(); i++) {
				idcg += (Math.pow(2, rels.get(i)))/log2(i+2);
			}
			IDCG.put(topic, idcg);
			
		}
	
	}
	
	public double log2(int i){
		return Math.log(i)/Math.log(2);
	}
	public double getNDCG(int iteration, String topic, String[][] docnos){
		
		double score = 0;
		int rank = 1;
		for (int i = 0; i < iteration; i++) {
			for (int j = 0; j < docnos[i].length; j++) {
				if (qrels.get(topic).containsKey(docnos[i][j])) {
					score += qrels.get(topic).get(docnos[i][j])/log2(rank+1);
				}
				rank++;
				
			}
		}
		//System.out.println();
		return score/IDCG.get(topic);
	}
}
