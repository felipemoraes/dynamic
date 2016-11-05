package br.ufmg.dcc.latin.feedback;

public class Passage {
	
	private String aspectId;
	private String text;
	private int relevance;
	
	public Passage(){
		
	}
	
	public Passage(String aspectId, String text, int relevance){
		this.aspectId = aspectId;
		this.text = text;
		this.relevance = relevance;
	}

	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getRelevance() {
		return relevance;
	}
	public void setRelevance(int relevance) {
		this.relevance = relevance;
	}
	public String getAspectId() {
		return aspectId;
	}
	public void setAspectId(String aspectId) {
		this.aspectId = aspectId;
	}
}
