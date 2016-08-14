package br.ufmg.dcc.latin.search.elements;

public class Feedback {

	private String docId;
	private Double score;
	private Subtopic[] subtopics;
	private Boolean onTopic;

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
	public Subtopic[] getSubtopics() {
		return subtopics;
	}
	public void setSubtopics(Subtopic[] subtopics) {
		this.subtopics = subtopics;
	}
	public Boolean getOnTopic() {
		return onTopic;
	}
	public void setOnTopic(Boolean onTopic) {
		this.onTopic = onTopic;
	}
}
