package br.ufmg.dcc.latin.system.session;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.SearchCache;
import br.ufmg.dcc.latin.diversity.FlatAspectManager;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.CollectionResultSet;
import br.ufmg.dcc.latin.querying.ESSearchRequest;
import br.ufmg.dcc.latin.querying.QueryRequest;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.ProportionalReranker;
import br.ufmg.dcc.latin.scoring.diversity.PM2;
import br.ufmg.dcc.latin.system.Evaluator;
import br.ufmg.dcc.latin.system.Session;
import br.ufmg.dcc.latin.system.TrecUser;

public class PM2Session implements Session {
	
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
		queryRequest.setSize(1000);
		this.topicId = topicId;
		baselineResultSet = (CollectionResultSet) searchRequest.search(queryRequest);
		SearchCache.cache(baselineResultSet);
		aspectManager = new FlatAspectManager(baselineResultSet.getDocsContent());
	}
	

	@Override
	public void run() {
		
		for (float[] params : getParameters()) {
			String runName = "PM2_" + (int) params[0] + "_" + String.format("%.1f" ,params[1]);
			run(runName,params);
		}

	}

	@Override
	public void run(String name, float[] params) {
		int depth = (int) params[0];
		float lambda = params[1];
		
		ResultSet resultSet = baselineResultSet;
		
		PM2 scorer = new PM2(AspectCache.v, AspectCache.s, AspectCache.coverage, lambda);
		ProportionalReranker reranker = new ProportionalReranker(scorer, depth, lambda);
	
		ResultSet topResultSet = reranker.getTopResults(resultSet);
		int iteration = 0;
		while (iteration < 10 & topResultSet.getDocids().length > 0) {
			Evaluator.writeToFile(name, topicId, topResultSet, iteration);
			Feedback[] feedbacks = TrecUser.get(topResultSet,topicId);
			aspectManager.miningProportionalAspects(feedbacks);
			scorer.update(AspectCache.v, AspectCache.s, AspectCache.coverage);
			reranker.updateProportional();
			resultSet = reranker.reranking(baselineResultSet);
			topResultSet = reranker.getTopResults(resultSet);
			iteration++;
		}
		aspectManager.clear();

	}

	@Override
	public List<float[]> getParameters() {
		int[] depths = {1000};
		float[] lambdas = {0.1f,0.2f,0.3f,0.4f,0.5f,0.6f,0.7f,0.8f,0.9f,1.0f};
		List<float[]> params = new ArrayList<float[]>();
		for (int i = 0; i < depths.length; i++) {
			for (int j = 0; j < lambdas.length; j++) {
				float[] param = {(int) depths[i], lambdas[j]};
				params.add(param);
			}
		}
 		return params;
	}

}
