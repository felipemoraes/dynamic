package br.ufmg.dcc.latin.system;

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
import br.ufmg.dcc.latin.reranking.DiversityReranker;
import br.ufmg.dcc.latin.scoring.diversity.xQuAD;

public class xQuADSession implements Session {
	
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
			String runName = "xQuAD_" + (int) params[0] + "_" + String.format("%.1f" ,params[1]);
			run(runName,params);
		}
		
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


	@Override
	public void run(String name, float[] params) {
		
		int depth = (int) params[0];
		float lambda = params[1];
		
		ResultSet resultSet = baselineResultSet;
		
		xQuAD scorer = new xQuAD(AspectCache.importance, AspectCache.coverage, AspectCache.novelty);
		DiversityReranker reranker = new DiversityReranker(scorer, depth, lambda);
	
		ResultSet topResultSet = reranker.getTopResults(resultSet);
		int iteration = 0;
		while (iteration < 10 & topResultSet.getDocids().length > 0) {
			Evaluator.writeToFile(name, topicId, topResultSet, iteration);
			Feedback[] feedbacks = TrecUser.get(topResultSet,topicId);
			aspectManager.miningDiversityAspects(feedbacks);
			aspectManager.updateNovelty(reranker.getSelected());
			scorer.update(AspectCache.importance, AspectCache.coverage, AspectCache.novelty);
			resultSet = reranker.reranking(baselineResultSet);
			topResultSet = reranker.getTopResults(resultSet);
			iteration++;
		}
		aspectManager.clear();
	}



}
