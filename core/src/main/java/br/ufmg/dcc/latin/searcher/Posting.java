/**
 * 
 */
package br.ufmg.dcc.latin.searcher;


/**
 * @author Felipe Moraes
 *
 */
public class Posting {

	private String[] terms;
	private int[] termFrequency;
	private long[] docFrequency;
	private long[] totalTermFrequency;
	private long docLen;
	private long docCount;
	private long sumTotalTermFrequency;


	public long getDocLen() {
		return docLen;
	}
	public void setDocLen(long docLen) {
		this.docLen = docLen;
	}
	public long getDocCount() {
		return docCount;
	}
	public void setDocCount(long docCount) {
		this.docCount = docCount;
	}
	public long getSumTotalTermFrequency() {
		return sumTotalTermFrequency;
	}
	public void setSumTotalTermFrequency(long sumTotalTermFrequency) {
		this.sumTotalTermFrequency = sumTotalTermFrequency;
	}
	public String[] getTerms() {
		return terms;
	}
	public void setTerms(String[] terms) {
		this.terms = terms;
	}
	public int[] getTermFrequency() {
		return termFrequency;
	}
	public void setTermFrequency(int[] termFrequency) {
		this.termFrequency = termFrequency;
	}
	public long[] getDocFrequency() {
		return docFrequency;
	}
	public void setDocFrequency(long[] docFrequency) {
		this.docFrequency = docFrequency;
	}
	public long[] getTotalTermFrequency() {
		return totalTermFrequency;
	}
	public void setTotalTermFrequency(long[] totalTermFrequency) {
		this.totalTermFrequency = totalTermFrequency;
	}
}
