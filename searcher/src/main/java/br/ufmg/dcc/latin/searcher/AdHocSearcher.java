package br.ufmg.dcc.latin.searcher;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.searcher.similarity.Similarity;

public abstract class AdHocSearcher {
	
	protected String indexName;
	protected List<String> searchPool;
	private Integer counter;
	
	public Integer getCounter() {
		return counter;
	}
	public void setCounter(Integer counter) {
		this.counter = counter;
	}

	private Similarity similarity;
	
	public Similarity getSimilarity() {
		return similarity;
	}
	public void setSimilarity(Similarity similarity) {
		this.similarity = similarity;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public AdHocSearcher(String indexName, Similarity similarity) throws UnknownHostException {
		this.indexName = indexName;
		this.counter = 0;
		this.similarity = similarity;
		searchPool = new ArrayList<String>();
		SimilarityModule similarityModule = new SimilarityModule();
		similarityModule.changeSimilarityModule(this.indexName, this.similarity);
	}
	
	public abstract void search(String query);
	
	public String getNextResults(){
		String result = "";
		if (searchPool.size() == counter){
			return null;
		}
		for (int i = counter; i < counter + 5; i++) {
			result += searchPool.get(i) + " ";
		}
		counter += 5;
		return result;
	}
}
