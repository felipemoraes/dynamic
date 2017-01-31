package br.ufmg.dcc.latin.utils;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class DocVec {
	public TIntIntMap vec;
	public int docLen;
	public DocVec(){
		vec = new TIntIntHashMap();
		docLen = 0;
	}
	
	public void add(int termId, int freq){
		vec.put(termId, freq);
		docLen += freq;
	}

	
}
