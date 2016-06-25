/**
 * 
 */
package br.ufmg.dcc.latin.simulator;

import java.util.List;

/**
 * @author Felipe Moraes
 *
 */
public class DDFeedback extends Feedback {
		/**
	 * @param topicId
	 * @param rankingScore
	 * @param onTopic
	 * @param docId
	 * @param subtopics
	 */
	public DDFeedback(String topicId, Float rankingScore, String onTopic, String docId, List<Subtopic> subtopics) {
		super();
		this.topicId = topicId;
		this.rankingScore = rankingScore;
		this.onTopic = onTopic;
		this.docId = docId;
		this.subtopics = subtopics;
	}
		/**
		 * 
		 */
		public DDFeedback() {
			// TODO Auto-generated constructor stub
		}
		private String topicId;
		private Float rankingScore;
		private String onTopic;
		private String docId;
		private List<Subtopic> subtopics;
		
	
		public String getTopicId() {
			return topicId;
		}
		public void setTopicId(String topicId) {
			this.topicId = topicId;
		}
		public Float getRankingScore() {
			return rankingScore;
		}
		public void setRankingScore(Float rankingScore) {
			this.rankingScore = rankingScore;
		}
		public String getOnTopic() {
			return onTopic;
		}
		public void setOnTopic(String onTopic) {
			this.onTopic = onTopic;
		}
		public String getDocId() {
			return docId;
		}
		public void setDocId(String docId) {
			this.docId = docId;
		}
		public List<Subtopic> getSubtopics() {
			return subtopics;
		}
		public void setSubtopics(List<Subtopic> subtopics) {
			this.subtopics = subtopics;
		}
}
