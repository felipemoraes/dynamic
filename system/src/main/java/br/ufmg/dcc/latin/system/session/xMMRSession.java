package br.ufmg.dcc.latin.system.session;

import java.util.List;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.SearchCache;
import br.ufmg.dcc.latin.diversity.FlatAspectManager;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.CollectionResultSet;
import br.ufmg.dcc.latin.querying.ESSearchRequest;
import br.ufmg.dcc.latin.querying.QueryRequest;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.xMMRReranker;
import br.ufmg.dcc.latin.system.Evaluator;
import br.ufmg.dcc.latin.system.Session;
import br.ufmg.dcc.latin.system.TrecUser;

public class xMMRSession implements Session {
	
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
		aspectManager = new FlatAspectManager(baselineResultSet.getDocsContent());
	}

	@Override
	public void run() {
		
		for (float[] params : getParameters()) {
			String runName = "xMMR_" + (int) params[0] + "_" + String.format("%.1f" ,params[1]);
			run(runName,params);
		}

		
	}

	@Override
	public void run(String name, float[] params) {
		int depth = (int) params[0];
		float lambda = params[1];
		
		ResultSet resultSet = baselineResultSet;
		
		
		xMMRReranker reranker = new xMMRReranker(depth, lambda);
		
		
		ResultSet topResultSet = reranker.getTopResults(resultSet);

		int iteration = 0;
		while (iteration < 10 & topResultSet.getDocids().length > 0) {
			
			Evaluator.writeToFile(name, topicId, topResultSet, iteration);
			Feedback[] feedbacks = TrecUser.get(topResultSet,topicId);
			aspectManager.miningDiversityAspects(feedbacks);
			reranker.setCoverage(AspectCache.coverage);
			topResultSet = reranker.getTopResults(baselineResultSet);
			
			iteration++;
		}
		aspectManager.clear();
		
	}

	@Override
	public List<float[]> getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

}
