package br.ufmg.dcc.latin.feedback;

public class Feedback {
	
	public String topicId;
	public String docno;
	public Passage[] passages;
	public boolean onTopic;
	public int index;
	
	public float getRelevanceAspect(String aspectId){
		int score = 0;
		if (!onTopic) {
			return 0;
		}
		for (int i = 0; i < passages.length; i++) {
			if (aspectId.equals(passages[i].aspectId)) {
				score = Math.max( passages[i].relevance, score);
			}
		}
		return (float) score/4.0f;
	}
	
 }
