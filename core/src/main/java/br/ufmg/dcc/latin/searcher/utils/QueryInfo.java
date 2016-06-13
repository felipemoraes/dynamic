/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

/**
 * @author Felipe Moraes
 *
 */
public class QueryInfo {
	private String text;
	private String id;
	private String indexName;
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
}
