package br.ufmg.dcc.latin.system.session;

import java.util.List;

import br.ufmg.dcc.latin.cache.SearchCache;
import br.ufmg.dcc.latin.diversity.FlatAspectManager;
import br.ufmg.dcc.latin.querying.CollectionResultSet;
import br.ufmg.dcc.latin.querying.ESSearchRequest;
import br.ufmg.dcc.latin.querying.QueryRequest;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.StaticReranker;
import br.ufmg.dcc.latin.system.Evaluator;
import br.ufmg.dcc.latin.system.Session;

public class LMSession implements Session{
	
	private CollectionResultSet baselineResultSet;
	FlatAspectManager aspectManager;
	private String topicId;


	@Override
	public void start(String index, String topicId, String topic) {
		ESSearchRequest searchRequest = ESSearchRequest.getInstance();
		QueryRequest queryRequest = new QueryRequest();
		queryRequest.setDocType("doc");
		float[] weights = {0.7f,0.3f};
		String[] fields = {"content", "title"};
		
		queryRequest.setFieldWeights(weights);
		queryRequest.setFields(fields);
		queryRequest.setQuery(topic);
		queryRequest.setIndex(index);
		queryRequest.setSize(100);
		this.topicId = topicId;
		baselineResultSet = (CollectionResultSet) searchRequest.search(queryRequest);
		SearchCache.cache(baselineResultSet);

		
	}

	@Override
	public void run() {
		
		ResultSet resultSet = baselineResultSet;
		String name = "LM";
		
		StaticReranker reranker = new StaticReranker();

		ResultSet topResultSet = reranker.getTopResults(resultSet);
		int iteration = 0;
		while (iteration < 10 & topResultSet.getDocids().length > 0) {
			Evaluator.writeToFile(name, topicId, topResultSet, iteration);
			topResultSet = reranker.getTopResults(resultSet);
			iteration++;
		}
		
	}

	@Override
	public void run(String name, float[] params) {
		
		
	}

	@Override
	public List<float[]> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
