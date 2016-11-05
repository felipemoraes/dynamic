package br.ufmg.dcc.latin.querying;

public class CollectionResultSet implements ResultSet{

	private int[] docids;
	private float[] scores;
	private String[] docnos;
	private String[] docsContent;

	@Override
	public int[] getDocids() {
		return docids;
	}

	@Override
	public float[] getScores() {
		return scores;
	}

	@Override
	public void setDocids(int[] docids) {
		this.docids = docids;
		
	}

	@Override
	public void setScores(float[] scores) {
		this.scores = scores;
	}

	@Override
	public String[] getDocnos() {
		return docnos;
	}

	@Override
	public void setDocnos(String[] docnos) {
		this.docnos = docnos;
	}

	public String[] getDocsContent() {
		return docsContent;
	}

	public void setDocsContent(String[] docsContent) {
		this.docsContent = docsContent;
	}

}
