package br.ufmg.dcc.latin.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.latin.baselineranker.ResultList;
import br.ufmg.dcc.latin.user.Passage;
import br.ufmg.dcc.latin.user.RelevanceSet;

public class RunWriter {
	
	private Map<String, RelevanceSet> repository;
	
	public RunWriter(){
		repository = new HashMap<String,RelevanceSet>();
		try (BufferedReader br = new BufferedReader(new FileReader("../share/truth_data.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(",",5);
		    	Passage passage = new Passage(splitLine[2],Integer.parseInt(splitLine[4]),Integer.parseInt(splitLine[3]));
		    	if (passage.relevance == 0 ) {
		    		passage.relevance = 1;
		    	}
		    	if (!repository.containsKey(splitLine[0])){
		    		repository.put(splitLine[0], new RelevanceSet());
		    	} 
		    	
		    	repository.get(splitLine[0]).add(splitLine[1], passage);
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public void writeToTrecDDFormat(String filename, String topicId, ResultList resultList, int iteration){
		
		try(FileWriter fw = new FileWriter("data/" + filename + ".txt", true);
			BufferedWriter bw = new BufferedWriter(fw);
			PrintWriter out = new PrintWriter(bw)) {
			String[] docnos = resultList.docnos;
			for (int i = 0; i < docnos.length; i++) {
				if (docnos[i] == null){
					continue;
				}
				float score = (float) ((1000.0-iteration-i)/1000.0);
				String wline = topicId + "\t" + iteration +"\t" + docnos[i] + "\t" + String.format("%.12f", score) + "\t";
				if (!repository.containsKey(docnos[i])) {
					wline += "0\tNULL";
				} else {
					Passage[] passages = repository.get(docnos[i]).get(topicId);
					if (passages == null) {
						wline += "0\tNULL";
					} else {
						
					
						wline += "1" +  "\t";
						for (int j = 0; j < passages.length; j++) {
							
							wline += passages[j].aspectId + ":" + passages[j].aspectId + "|";
						}
						wline = wline.substring(0,wline.length()-1);
					}
				}
				out.println(wline);
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
}
