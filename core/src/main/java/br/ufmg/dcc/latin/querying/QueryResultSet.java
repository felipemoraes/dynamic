package br.ufmg.dcc.latin.querying;

public class QueryResultSet implements ResultSet {
	
	private int[] docids;
	private float[] scores;
	

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setDocnos(String[] docnos) {
		// TODO Auto-generated method stub
		
	}

}
