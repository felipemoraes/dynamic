package br.ufmg.dcc.latin.models;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.latin.search.elements.Feedback;


public class HxQuADReRanker extends HxQuAD {

	public double[][][][] coverage;
	public double[][][] importance;
	public double[] ambiguity;
	private double[] weight;
	public int depth;
	String topicId;
	
	
	private ReRankerResource resource = new ReRankerResource();
	
	public void start(String index, String query, String topicId, double[]  ambiguity, int depth, double[] weight, String stopType) {
		getSelected().clear();
		resource.create(index,query,stopType);
		
		this.importance = null;
		this.coverage = null;
		this.ambiguity = ambiguity;
		this.depth = depth;
		this.weight = weight;
		this.topicId = topicId;
		
	}
	
	
	public Map<String, Double> get() {
		getCurrentSelected().clear();

		HashMap<String,Double> result = new HashMap<String,Double>();
		if (resource.stop()) {
			return result;
		}
		
		score(resource.docIds,resource.relevance,coverage,
				importance,weight,ambiguity,depth);

		
		for (int i = 0; i < resource.docIds.length; ++i){
			if (getCurrentSelected().containsKey(resource.docIds[i])) {
				result.put(resource.docNos[i],getCurrentSelected().get(resource.docIds[i]));
			}
		}
		return result;
	}
	
	
	public void updateStoppingRules(Feedback[] feedback){
		resource.updateRules(feedback);
	}
	
	public void updateSuggestionsFeedback(Feedback[] feedback, String suggestionsFilename, String topicId){
		double[][][] coverageFeedback =  resource.getCoverageWithFeedback(feedback);
		double[][][] coverageSuggestions =  resource.getHierarchicalCoverageWithSuggestions(topicId,suggestionsFilename);
		double[][][][] coverageAux = new double[2][][][];
		coverageAux[0] = coverageSuggestions;
		coverageAux[1] = coverageFeedback;
		update(coverageAux);
	}
	
	public void update(double[][][][] coverage) {
		this.coverage = coverage;
		updateNovelty(resource.docIds,coverage);
		importance = new double[coverage.length][][];
		for (int m = 0; m < coverage.length; ++ m) {
			if (coverage[m] == null) {
				continue;
			}
			importance[m] = new double[coverage[m][0].length][];
			for (int i = 0 ; i < coverage[m][0].length;++i ){
				importance[m][i] = new double[coverage[m][0][i].length];
				Arrays.fill(importance[m][i], 1);
			}
		}
	}
	
	public void updateFeedback(Feedback[] feedback){
		double[][][] coverage =  resource.getCoverageWithFeedback(feedback);
		double[][][][] coverageAux = new double[1][][][];
		coverageAux[0] = coverage;
		update(coverageAux);
	}


	public void updateSuggestions(String suggestionsFilename) {
		double[][][] coverage =  resource.getHierarchicalCoverageWithSuggestions(topicId, suggestionsFilename);
		double[][][][] coverageAux = new double[1][][][];
		coverageAux[0] = coverage;
		update(coverageAux);
		
	}


	public ReRankerResource getResource() {
		return resource;
	}


	public void setResource(ReRankerResource resource) {
		this.resource = resource;
	}


	public double[] getWeight() {
		return weight;
	}


	public void setWeight(double[] weight) {
		this.weight = weight;
	}

}
