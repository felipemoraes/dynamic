package br.ufmg.dcc.latin.querying;

public class BooleanSelectedSet {
	boolean[] selected;
	
	int size;
	public BooleanSelectedSet(int n) {
		selected = new boolean[n];
	}
	public boolean has(int d){
		return selected[d];
	}
	
	public void put(int d){
		if (selected[d] == false) {
			selected[d] = true;
			size++;
		}
	}
	
	public int size(){
		return size;
	}
}
