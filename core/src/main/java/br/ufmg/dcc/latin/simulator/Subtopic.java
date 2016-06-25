/**
 * 
 */
package br.ufmg.dcc.latin.simulator;

/**
 * @author Felipe Moraes
 *
 */
public class Subtopic {

	/**
	 * @param subtopicId
	 * @param rating
	 * @param passageText
	 */
	
	private String subtopicId;
	private Integer rating;
	private String passageText;
	
	public Subtopic(String subtopicId, Integer rating, String passageText) {
		this.setSubtopicId(subtopicId);
		this.setRating(rating);
		this.setPassageText(passageText);
		
	}

	public String getSubtopicId() {
		return subtopicId;
	}

	public void setSubtopicId(String subtopicId) {
		this.subtopicId = subtopicId;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public String getPassageText() {
		return passageText;
	}

	public void setPassageText(String passageText) {
		this.passageText = passageText;
	}

}
