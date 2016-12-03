package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NgramCollection {

	private long sumTotalTermFreq;
	
	private Map<String,Integer> totalTermFreq;
	
	public NgramCollection(){
	
		totalTermFreq =  new HashMap<String,Integer>();
		
		try (BufferedReader br = new BufferedReader(new FileReader("../share/googlengram_counts.txt"))) {
			String line = br.readLine();

			sumTotalTermFreq = Long.parseLong(line);
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ",2);
		    	totalTermFreq.put(splitLine[0], Integer.parseInt(splitLine[1]));
		    			
			}
			

		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public int getTotalTermFreq(String term){
		return totalTermFreq.getOrDefault(term, 0);
	}

	public long getSumTotalTermFreq() {
		return sumTotalTermFreq;
	}

	public void setSumTotalTermFreq(int sumTotalTermFreq) {
		this.sumTotalTermFreq = sumTotalTermFreq;
	}
	
	
}
