package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.util.BytesRef;

public class NgramCollection {

	private long sumTotalTermFreq;
	
	private Map<BytesRef,Integer> totalTermFreq;
	
	public NgramCollection(){
	
		totalTermFreq =  new HashMap<BytesRef,Integer>();
		
		try (BufferedReader br = new BufferedReader(new FileReader("../share/googlengram_counts.txt"))) {
			String line = br.readLine();

			sumTotalTermFreq = Long.parseLong(line);
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ",2);
		    	totalTermFreq.put(new BytesRef(splitLine[0]), Integer.parseInt(splitLine[1]));
		    			
			}
			

		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public int getTotalTermFreq(BytesRef term){
		return totalTermFreq.getOrDefault(term, 0);
	}

	public long getSumTotalTermFreq() {
		return sumTotalTermFreq;
	}

	public void setSumTotalTermFreq(int sumTotalTermFreq) {
		this.sumTotalTermFreq = sumTotalTermFreq;
	}
	
	
}
