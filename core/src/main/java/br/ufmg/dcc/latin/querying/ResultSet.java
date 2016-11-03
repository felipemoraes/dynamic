package br.ufmg.dcc.latin.querying;

public interface ResultSet {
	public int[] getDocids();
	public float[] getScores();
	
	public void setDocids(int[] docids);
	public void setScores(float[] scores);
}
