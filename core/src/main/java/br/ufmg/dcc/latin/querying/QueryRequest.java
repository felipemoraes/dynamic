package br.ufmg.dcc.latin.querying;

public class QueryRequest {
	
	private String query;
	private String index;
	private String docType;
	private String[] fields;
	private float[] fieldWeights;
	private int size;
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}
	public String[] getFields() {
		return fields;
	}
	public void setFields(String[] fields) {
		this.fields = fields;
	}
	public float[] getFieldWeights() {
		return fieldWeights;
	}
	public void setFieldWeights(float[] fieldWeights) {
		this.fieldWeights = fieldWeights;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}

}
