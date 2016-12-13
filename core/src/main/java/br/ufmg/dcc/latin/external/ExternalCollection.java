package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class ExternalCollection {
	
	private int numDocs;
	private int sumDocFreq;
	private int sumTotalTermFreq;
	
	private  int[] docFreq;
	private int[] totalTermFreq;
	
	public ExternalCollection(String filename){
		int n = RetrievalController.vocab[0].size();
		docFreq = new int[n];
		totalTermFreq =  new int[n];
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
			
			String[] splitLine = line.split(" ",3);
			numDocs = Integer.parseInt(splitLine[0]);
			sumDocFreq = Integer.parseInt(splitLine[1]);
			setSumTotalTermFreq(Integer.parseInt(splitLine[2]));
			while ((line = br.readLine()) != null) {
		    	splitLine = line.split(" ",3);
		    	String term = splitLine[0];
		    	int termId = RetrievalController.vocab[0].getId(term);
		    	if (termId == -1) {
		    		continue;
		    	}
		    	docFreq[termId] =  Integer.parseInt(splitLine[1]);
		    	totalTermFreq[termId] = Integer.parseInt(splitLine[2]);
		    			
			}
			

		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public int getDocFreq(int termId){
		return docFreq[termId];
	}
	
	public int getTotalTermFreq(int termId){
		return totalTermFreq[termId];
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
