/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

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
	public DDFeedback(String topicId, Double rankingScore, String onTopic, String docId, List<Subtopic> subtopics) {
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
		private Double rankingScore;
		private String onTopic;
		private String docId;
		private List<Subtopic> subtopics;
		
	
		public String getTopicId() {
			return topicId;
		}
		public void setTopicId(String topicId) {
			this.topicId = topicId;
		}
		public Double getRankingScore() {
			return rankingScore;
		}
		public void setRankingScore(Double rankingScore) {
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
