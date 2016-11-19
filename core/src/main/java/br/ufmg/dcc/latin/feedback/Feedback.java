package br.ufmg.dcc.latin.feedback;

public class Feedback {
	
	private String topicId;
	private String docno;
	private Passage[] passages;
	private boolean onTopic;
	
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	
	public Passage[] getPassages() {
		return passages;
	}
	
	public float getRelevanceAspect(String aspectId){
		int score = 0;
		if (!onTopic) {
			return 0;
		}
		for (int i = 0; i < passages.length; i++) {
			if (aspectId.equals(passages[i].getAspectId())) {
				score = Math.max( passages[i].getRelevance(), score);
			}
		}
		return (float) score/4.0f;
	}
	
	public void setPassages(Passage[] passages) {
		this.passages = passages;
	}
	
	public boolean isOnTopic() {
		return onTopic;
	}
	public void setOnTopic(boolean onTopic) {
		this.onTopic = onTopic;
	}
	public String getDocno() {
		return docno;
	}
	public void setDocno(String docno) {
		this.docno = docno;
	}
	
 }
