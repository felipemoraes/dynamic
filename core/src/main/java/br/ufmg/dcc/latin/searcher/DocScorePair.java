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
	private Double score;
	
	/**
	 * @param id
	 * @param score2
	 */
	public DocScorePair(String docId, Double score) {
		this.docId = docId;
		this.score = score;
	}
	public String getDocId() {
		return docId;
	}
	public void setDocId(String docId) {
		this.docId = docId;
	}
	public Double getScore() {
		return score;
	}
	public void setScore(Double score) {
		this.score = score;
	}
}
