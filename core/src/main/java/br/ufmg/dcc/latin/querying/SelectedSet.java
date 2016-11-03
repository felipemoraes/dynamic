package br.ufmg.dcc.latin.querying;

import java.util.HashSet;
import java.util.Set;

public class SelectedSet {
	Set<Integer> set;
	public SelectedSet(){
		set = new HashSet<Integer>();
	}
	
	public boolean has(int d){
		return set.contains(d);
	}
	
	public void put(int d){
		set.add(d);
	}

	public int size() {
		return set.size();
	}
	
}
