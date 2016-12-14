package br.ufmg.dcc.latin.feedback;

public class Passage {
	
	public String aspectId;
	public int passageId;
	public int relevance;
	
	public Passage(){
		
	}
	
	public Passage(String aspectId, int passageId, int relevance){
		this.aspectId = aspectId;
		this.passageId = passageId;
		this.relevance = relevance;
	}
}
