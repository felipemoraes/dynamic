package br.ufmg.dcc.latin.querying;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class SelectedSet {
	Set<String> set;
	public SelectedSet(){
		set = new HashSet<String>();
	}
	
	public boolean has(String d){
		return set.contains(d);
	}
	
	public void put(String d){
		set.add(d);
	}
	
	public List<String> getAll(){
		List<String> all = new ArrayList<String>();
		Iterator<String> it = set.iterator();
		while(it.hasNext()){
			all.add(it.next());
		}
		return all;
	}

	public int size() {
		return set.size();
	}
	
}
