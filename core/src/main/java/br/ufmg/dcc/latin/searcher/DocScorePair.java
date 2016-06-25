/**
 * 
 */
package br.ufmg.dcc.latin.searcher;

/**
 * @author Felipe Moraes
 *
 */
public class DocScorePair {
	private String docId;
	private Float score;
	
	/**
	 * @param id
	 * @param score2
	 */
	public DocScorePair(String docId, Float score) {
		this.docId = docId;
		this.score = score;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public Float getScore() {
		return score;
	}
	public void setScore(Float score) {
		this.score = score;
	}
}
