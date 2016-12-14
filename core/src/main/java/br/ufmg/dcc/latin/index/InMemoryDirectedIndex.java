package br.ufmg.dcc.latin.index;

import gnu.trove.list.array.TIntArrayList;

public class InMemoryDirectedIndex {
	
	public DocVec[] docVecs;
	public long sumTotalTermFreq;
	public long sumDocFreq;
	public int docCount;
	public TIntArrayList[] invertedIndex;
	
	
	public InMemoryDirectedIndex(int vocabSize, int docCount,  long sumTotalTermFreq, long sumDocFreq) {
		docVecs = new DocVec[1000];
		this.docCount = docCount;
		this.sumTotalTermFreq = sumTotalTermFreq;
		this.sumDocFreq = sumDocFreq;
		invertedIndex = new TIntArrayList[vocabSize];
		for (int i = 0; i < vocabSize; i++) {
			invertedIndex[i] = new TIntArrayList();
		}
	}
}
