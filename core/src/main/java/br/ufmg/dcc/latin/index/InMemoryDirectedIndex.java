package br.ufmg.dcc.latin.index;

public class InMemoryDirectedIndex {
	
	public DocVec[] docVecs;
	long sumTotalTermFreq;
	long sumDocFreq;
	int docCount;
	
	public InMemoryDirectedIndex(int vocabSize, int docCount,  long sumTotalTermFreq, long sumDocFreq) {
		docVecs = new DocVec[1000];
		this.docCount = docCount;
		this.sumTotalTermFreq = sumTotalTermFreq;
		this.sumDocFreq = sumDocFreq;
	}

	public long getDocCount(){
		return docCount;
	}


	public double sumTotalTerms() {
		return sumDocFreq;
	}

	public long sumTotalTermsFreq() {
		return sumTotalTermFreq;
	}
}
