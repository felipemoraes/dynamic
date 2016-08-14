package br.ufmg.dcc.latin.search.elements;

public class Subtopic {
	private String id;
	private Integer rating;
	private String passageText;
	
	public Subtopic(String id, Integer rating, String passageText){
		this.id = id;
		this.rating = rating;
		this.passageText = passageText;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPassageText() {
		return passageText;
	}
	public void setPassageText(String passageText) {
		this.passageText = passageText;
	}
	public Integer getRating() {
		return rating;
	}
	public void setRating(Integer rating) {
		this.rating = rating;
	}
}
