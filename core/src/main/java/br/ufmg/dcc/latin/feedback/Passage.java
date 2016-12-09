package br.ufmg.dcc.latin.feedback;

public class Passage {
	
	private String aspectId;
	private int passageId;
	private int relevance;
	
	public Passage(){
		
	}
	
	public Passage(String aspectId, int passageId, int relevance){
		this.aspectId = aspectId;
		this.passageId = passageId;
		this.relevance = relevance;
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

	public int getPassageId() {
		return passageId;
	}

	public void setPassageId(int passageId) {
		this.passageId = passageId;
	}
}
