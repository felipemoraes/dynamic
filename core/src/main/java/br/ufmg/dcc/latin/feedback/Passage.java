package br.ufmg.dcc.latin.feedback;

public class Passage {
	
	public String subtopicId;
	public int passageId;
	public int relevance;
	
	public Passage(){
		
	}
	
	public Passage(String subtopicId, int passageId, int relevance){
		this.subtopicId = subtopicId;
		this.passageId = passageId;
		this.relevance = relevance;
	}
}
