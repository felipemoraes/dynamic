/**
 * 
 */
package br.ufmg.dcc.latin.searcher;

/**
 * @author Felipe Moraes
 *
 */
public class ResultSet {
	private int[] docIds;
	private float[] scores;
	//Optional
	
	private Posting[] postings;
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
	public Posting[] getPostings() {
		return postings;
	}
	public void setPostings(Posting[] postings) {
		this.postings = postings;
	}
}
