/**
 * 
 */
package br.ufmg.dcc.latin.searcher;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Felipe Moraes
 *
 */
public class ResultSet {
	private int[] docIds;
	private String[] docNos;
	private float[] scores;
	//Optional
	
	private Posting[][] postings;
	private String[] docContent;
	public int[] getDocIds() {
		return docIds;
	}
	public void setDocIds(int[] docIds) {
		this.docIds = docIds;
	}
	public float[] getScores() {
		return scores;
	}
	public void setScores(float[] scores) {
		this.scores = scores;
	}
	public Posting[][] getPostings() {
		return postings;
	}
	public void setPostings(Posting[][] postings) {
		this.postings = postings;
	}
	public String[] getDocNos() {
		return docNos;
	}
	public void setDocNos(String[] docNos) {
		this.docNos = docNos;
	}
	public String[] getDocContent() {
		return docContent;
	}
	public void setDocContent(String[] docContent) {
		this.docContent = docContent;
	}
	
	public void readFromFile(String query){
		try (BufferedReader br = new BufferedReader(new FileReader("data/" + query + ".data"))){
			int n = Integer.parseInt(br.readLine());
			docIds = new int[n];
			docNos = new String[n];
			scores = new float[n];
			docContent = new String[n];
			for (int i = 0; i < n; ++i) {
				String line = br.readLine();
				if (line.length() == 0) {
					continue;
				}
				String[] splitLine = line.split(",",4);
				docIds[i] = Integer.parseInt(splitLine[0]);
				docNos[i] = splitLine[1];
				scores[i] = Float.parseFloat(splitLine[2]);
				docContent[i] = splitLine[3];
			}
		} catch (IOException e) {
			
		}
	}
	
	public void writeToFile(String query){
		try (FileWriter fw = new FileWriter("data/" + query + ".data");
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)){
			out.println(docIds.length);
			for (int i = 0; i < docIds.length; ++i){
				
				docContent[i] = docContent[i].replace("\r", " ").replace("\n", " ");
				out.println(docIds[i]+"," + docNos[i] + "," + scores[i] + "," + docContent[i]);
			}
		
		} catch (IOException e) {
			
		}
	}
}
