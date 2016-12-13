package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class NgramCollection {

	private long sumTotalTermFreq;
	
	private int[] totalTermFreq;
	
	public NgramCollection(){
		int n = RetrievalController.vocab[0].size();
		totalTermFreq = new int[n];
		
		try (BufferedReader br = new BufferedReader(new FileReader("../share/googlengram_counts.txt"))) {
			String line = br.readLine();

			sumTotalTermFreq = Long.parseLong(line);
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ",2);
		    	int termId = RetrievalController.vocab[0].getId(splitLine[0]);
		    	if (termId == -1) {
		    		continue;
		    	}
		    	totalTermFreq[termId] = Integer.parseInt(splitLine[1]);
			}
			

		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public int getTotalTermFreq(int termId){
		return totalTermFreq[termId];
	}

	public long getSumTotalTermFreq() {
		return sumTotalTermFreq;
	}

	public void setSumTotalTermFreq(int sumTotalTermFreq) {
		this.sumTotalTermFreq = sumTotalTermFreq;
	}
	
	
}
