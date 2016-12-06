package br.ufmg.dcc.latin.retrieval;

import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.util.BytesRef;

public class TermStatistics {
	
	private Map<BytesRef,Double> docFreq;
	private Map<BytesRef,Double> totalTermFreq;
	
	public TermStatistics(){
		docFreq = new HashMap<BytesRef,Double>();
		totalTermFreq = new HashMap<BytesRef,Double>();
	}
	
	public double docFreq(BytesRef term){
		return docFreq.getOrDefault(term, 0d);
	}
	
	public double totalTermFreq(BytesRef term){
		return totalTermFreq.getOrDefault(term, 0d);
	}
	
	public void docFreq(BytesRef term, double docFreq){
		this.docFreq.put(term, docFreq);
	}
	
	public void totalTermFreq(BytesRef term, double totalTermFreq){
		this.totalTermFreq.put(term, totalTermFreq);
	}
}
