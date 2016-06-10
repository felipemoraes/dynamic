/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

/**
 * @author Felipe Moraes
 *
 */
public class DocumentTopicPair {
	/**
	 * @param documentId
	 * @param topicId
	 */
	public DocumentTopicPair(String documentId, String topicId) {
		this.documentId = documentId;
		this.topicId = topicId;
	}
	String documentId;
	String topicId;

}
