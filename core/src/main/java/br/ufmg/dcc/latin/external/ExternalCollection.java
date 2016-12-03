package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ExternalCollection {
	
	private int numDocs;
	private int sumDocFreq;
	private int sumTotalTermFreq;
	
	private Map<String,Integer> docFreq;
	private Map<String,Integer> totalTermFreq;
	
	public ExternalCollection(String filename){
		docFreq = new HashMap<String,Integer>();
		totalTermFreq =  new HashMap<String,Integer>();
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
			
			String[] splitLine = line.split(" ",3);
			numDocs = Integer.parseInt(splitLine[0]);
			sumDocFreq = Integer.parseInt(splitLine[1]);
			setSumTotalTermFreq(Integer.parseInt(splitLine[2]));
			while ((line = br.readLine()) != null) {
		    	splitLine = line.split(" ",3);
		    	docFreq.put(splitLine[0], Integer.parseInt(splitLine[1]));
		    	totalTermFreq.put(splitLine[1], Integer.parseInt(splitLine[2]));
		    			
			}
			

		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public int getDocFreq(String term){
		return docFreq.getOrDefault(term, 0);
	}
	
	public int getTotalTermFreq(String term){
		return totalTermFreq.getOrDefault(term, 0);
	}

	public int getNumDocs() {
		return numDocs;
	}

	public void setNumDocs(int numDocs) {
		this.numDocs = numDocs;
	}

	public int getSumDocFreq() {
		return sumDocFreq;
	}

	public void setSumDocFreq(int sumDocFreq) {
		this.sumDocFreq = sumDocFreq;
	}

	public int getSumTotalTermFreq() {
		return sumTotalTermFreq;
	}

	public void setSumTotalTermFreq(int sumTotalTermFreq) {
		this.sumTotalTermFreq = sumTotalTermFreq;
	}
}
