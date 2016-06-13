package br.ufmg.dcc.latin.searcher;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.searcher.models.WeightingModel;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;

public abstract class AdHocSearcher {
	
	protected String indexName;
	protected List<DocScorePair> searchPool;
	private Integer counter;
	
	public Integer getCounter() {
		return counter;
	}
	public void setCounter(Integer counter) {
		this.counter = counter;
	}

	private WeightingModel similarity;
	
	public WeightingModel getSimilarity() {
		return similarity;
	}
	public void setSimilarity(WeightingModel similarity) {
		this.similarity = similarity;
	}
	public String getIndexName() {
		return indexName;
	}
	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}
	public AdHocSearcher(String indexName, WeightingModel similarity) throws UnknownHostException {
		this.indexName = indexName;
		this.counter = 0;
		this.similarity = similarity;
		searchPool = new ArrayList<DocScorePair>();
		WeightingModule similarityModule = new WeightingModule();
		similarityModule.changeWeightingModel(this.indexName, this.similarity);
	}
	
	public abstract void search(String query);
	
	public ResultSet getNextResults(){
		if (searchPool.size() == counter){
			return null;
		}
		ResultSet result = new ResultSet();
		for (int i = counter; i < counter + 5; i++) {
			result.putResult(searchPool.get(i).getDocId(),searchPool.get(i).getScore());
		}
		counter += 5;
		return result;
	}
}
