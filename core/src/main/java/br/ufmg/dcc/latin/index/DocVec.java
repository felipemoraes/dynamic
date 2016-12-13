package br.ufmg.dcc.latin.index;

import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

public class DocVec {
	TIntIntMap vec;
	int docLen;
	public DocVec(){
		vec = new TIntIntHashMap();
		docLen = 0;
	}
	
	public void add(int termId, int freq){
		vec.put(termId, freq);
		docLen += freq;
	}
	
	public int[] getTerms(){
		return vec.keys();
	}
	
	public int getFreq(int termId){
		return vec.get(termId);
	}

	public int docLen() {
		return docLen;
	}
	
	public void docLen(int docLen) {
		this.docLen = docLen;
	}
	
	
}
