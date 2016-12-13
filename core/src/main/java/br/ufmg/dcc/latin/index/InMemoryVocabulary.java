package br.ufmg.dcc.latin.index;

import java.io.IOException;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import gnu.trove.map.hash.TObjectIntHashMap;

public class InMemoryVocabulary {
	
	TObjectIntHashMap<String> vocab;
	String[] invVocab;
	
	public InMemoryVocabulary(Terms terms) {
		try {
			int n = 0;
			TermsEnum iterator = terms.iterator();
			BytesRef term =  iterator.next();
			while (term != null) {
				n++;
				term = iterator.next();
			}
			vocab = new TObjectIntHashMap<String>();
			vocab.ensureCapacity(n+1000);
			invVocab = new String[n+1000];
			iterator = terms.iterator();
			term =  iterator.next();
			while (term != null) {
				this.addTerm(term.utf8ToString());
				term = iterator.next();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	
	public int addTerm(String term){
		int termId = vocab.size();
		vocab.putIfAbsent(term, termId);
		invVocab[termId] = term;
		return termId;
	}
	
	public int getId(String term){
		if (vocab.containsKey(term)) {
			return vocab.get(term);
		} else {
			return -1;
		}
	}
	
	public String getTerm(int termId){
		return invVocab[termId];
	}
	
	public boolean contains(String term){
		return vocab.contains(term);
	}
	
	public int size(){
		return vocab.size();
	}
	
}
