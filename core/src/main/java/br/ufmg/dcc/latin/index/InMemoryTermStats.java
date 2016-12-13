package br.ufmg.dcc.latin.index;

import java.io.IOException;

import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;



public class InMemoryTermStats {
	
	int[] docFreq;
	long[] totalTermFreq;
	
	public InMemoryTermStats(Terms terms, InMemoryVocabulary vocab) {
		try {
			int n = vocab.size();
			docFreq = new int[n];
			totalTermFreq = new long[n];
			TermsEnum iterator = terms.iterator();
			BytesRef term =  iterator.next();
			while (term != null) {
				String t = term.utf8ToString();
				int termId =  vocab.getId(t);
				if (termId != -1) {
					docFreq[termId] = iterator.docFreq();
					totalTermFreq[termId] = iterator.totalTermFreq();
				}
				term = iterator.next();
	
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public int docFreq(int termId){
		return docFreq[termId];
	}
	
	public long totalTermFreq(int termId){
		return totalTermFreq[termId];
	}
	
}
