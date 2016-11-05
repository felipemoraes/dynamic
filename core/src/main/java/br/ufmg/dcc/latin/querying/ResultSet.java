package br.ufmg.dcc.latin.querying;

public interface ResultSet {
	public int[] getDocids();
	public float[] getScores();
	public String[] getDocnos();
	
	public void setDocids(int[] docids);
	public void setScores(float[] scores);
	public void setDocnos(String[] docnos);
}
