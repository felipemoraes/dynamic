package br.ufmg.dcc.latin.querying;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
	
	public List<Integer> getAll(){
		List<Integer> all = new ArrayList<Integer>();
		Iterator<Integer> it = set.iterator();
		while(it.hasNext()){
			all.add(it.next());
		}
		return all;
	}

	public int size() {
		return set.size();
	}
	
}
