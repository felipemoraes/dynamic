package br.ufmg.dcc.latin.scoring;

public interface Scorer {
	public void build(float[] params);

	public float score(int docid);
	
	public void flush();
	
	public void update(int docid);
}
