package br.ufmg.dcc.latin.controlers;

import java.util.Map;
import java.util.TreeMap;

import br.ufmg.dcc.latin.models.xQuADi;
import br.ufmg.dcc.latin.models.xQuADiParameter;
import br.ufmg.dcc.latin.models.xQuADiParameterBuilder;
import br.ufmg.dcc.latin.search.elements.Feedback;

public class DynamicSearchController implements SearchController {
	
	private Feedback[][] feedbackCache;
	
	private xQuADi model;
	private xQuADiParameter[] parameters;
	
	private int nextToken;
	
	public DynamicSearchController(){
		feedbackCache = new Feedback[1000][];
		parameters = new xQuADiParameter[1000];
		nextToken = 0;
	}
	
	private int getNextToken(){
		if (nextToken == 1000){
			nextToken = 1;
			return 0;
		}
		nextToken++;
		return 0;
		//return nextToken-1;
	}
	
	
	public int initSearch(String index, String model, String query) {
		
		int token = getNextToken(); // create token in a table;
		this.model = new xQuADi();
		parameters[token] = xQuADiParameterBuilder.build(index, model,query);
		feedbackCache[token] = new Feedback[5000];
		return token;
	}

	
	@Override
	public SearchResponse searchQuery(String index, String model, String query, Integer token) {
		SearchResponse searchResponse = new SearchResponse();
		if (token == null){
			token = getNextToken(); // create token in a table
			this.model = new xQuADi();
			parameters[token] = xQuADiParameterBuilder.build(index, model,query);
			feedbackCache[token] = new Feedback[5000];
		}
		Map<String, Double> result = this.model.get(parameters[token]);
		searchResponse.setToken(token);
		searchResponse.setResponse(result);
		return searchResponse;
	}

	@Override
	public void updateFeedback(Feedback[] feedback, Integer token) {
		int j = 0;
		for(int i = 0; i < feedbackCache[token].length; ++i){
			if (j >= feedback.length) {
				break;
			}
			if (feedbackCache[token][i] == null){
				feedbackCache[token][i] = feedback[j];
				j++;
			}
		}
		parameters[token] = xQuADiParameterBuilder
				.rebuildWithFeedback(parameters[token], feedbackCache[token]);
	}

	public void setCoverage(double[][] coverage, int token){
		parameters[token].coverage = coverage;
		parameters[token].k = coverage[0].length;
		parameters[token].importance = new double[parameters[token].k];
		for (int i = 0; i < parameters[token].k; ++i){
			parameters[token].importance[i] = (double) 1/parameters[token].k;
		}
	}

	public int[] getDocIds(int token) {
		return parameters[token].docIds;
	}

	public String[] getDocNos(int token) {
		return parameters[token].docNos;
	}

	public void setImportance(double[] importance, int token) {
		parameters[token].importance = importance;
	}
}

