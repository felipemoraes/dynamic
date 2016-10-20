package br.ufmg.dcc.latin.models;


import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import br.ufmg.dcc.latin.search.elements.Feedback;


public class xQuADReRanker extends xQuAD {

	private double[][][] coverage;
	private double[][] importance;
	private double[] weight;
	private double ambiguity;
	private int depth;
	
	private String topicId;
	
	private ReRankerResource resource = new ReRankerResource();
	
	public void start(String index, String query, String topicId, double  ambiguity, int depth, double[] weight,String stopType) {
		getSelected().clear();
		resource.create(index, query,stopType);
		this.ambiguity = ambiguity;
		this.depth = depth;
		this.coverage = null;
		this.importance = null;
		this.weight = weight;
		this.topicId = topicId;
		
		

	}
	
	public Map<String, Double> get() {
		getCurrentSelected().clear();
		
		HashMap<String,Double> result = new HashMap<String,Double>();
		if (resource.stop()){
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
	
	public void update(double[][][] coverage){
		this.coverage = coverage;
		updateNovelty(resource.docIds,this.coverage);
		importance = new double[coverage.length][];
		
		for (int i = 0; i < coverage.length; i++) {
			if (coverage[i] != null) {
				importance[i] = new double[coverage[i][0].length];
				Arrays.fill(importance[i], 1);
			}
		}
		
	}
	
	
	public void updateWithMax(double[][][][] coverage) {
		this.coverage = new double[coverage.length][coverage[0].length][coverage[0][0].length];
		for (int m = 0; m < coverage.length; m++){
			for (int i = 0; i < coverage[m].length; i++) {
				for (int j = 0; j < coverage[m][i].length; j++) {
					double max = Double.NEGATIVE_INFINITY;
					for (int k = 0; k < coverage[m][i][j].length; k++) {
						max = Math.max(max, coverage[m][i][j][k]);
					}
					this.coverage[m][i][j] = max;
				}
			}
		}
		updateNovelty(resource.docIds,this.coverage);
		importance = new double[coverage.length][];
		for (int m = 0; m < coverage.length; m++){
			importance[m] = new double[coverage[m][0].length];
			Arrays.fill(importance[m], 1);
		}
		
	}
	
	private double[][] getCoverageWithMax(double[][][] coverage) {
		if (coverage == null) {
			return null;
		}
		double[][] coverageMax = new double[coverage.length][coverage[0].length];
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[i].length; j++) {
				double max = Double.NEGATIVE_INFINITY;
				for (int k = 0; k < coverage[i][j].length; k++) {
					max = Math.max(max, coverage[i][j][k]);
				}
				coverageMax[i][j] = max;
			}
		}
		return coverageMax;
	}
	

	public void updateWithMean(double[][][][] coverage) {
		
		this.coverage = new double[coverage.length][coverage[0].length][coverage[0][0].length];
		for (int m = 0; m < coverage.length; m++){
			for (int i = 0; i < coverage[m].length; i++) {
				for (int j = 0; j < coverage[m][i].length; j++) {
					double sum = 0;
					int c = 0;
					for (int k = 0; k < coverage[m][i][j].length; k++) {
						if (coverage[m][i][j][k]> 0) {
							sum += coverage[m][i][j][k];
							c++;
						}
					}
					this.coverage[m][i][j] = 0;
					if (c > 0) {
						this.coverage[m][i][j] = sum/c;
					}
				}
			}
		}
		
		updateNovelty(resource.docIds,this.coverage);
		importance = new double[coverage.length][];
		for (int m = 0; m < coverage.length; m++){
			importance[m] = new double[coverage[m][0].length];
			Arrays.fill(importance[m], 1);
		}
	}
	
	private double[][] getCoverageWithMean(double[][][] coverage) {
		if (coverage == null) {
			return null;
		}
		double[][] coverageMean = new double[coverage.length][coverage[0].length];
		
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[i].length; j++) {
				double sum = 0;
				int c = 0;
				for (int k = 0; k < coverage[i][j].length; k++) {
					if (coverage[i][j][k]> 0) {
						sum += coverage[i][j][k];
						c++;
					}
				}
				coverageMean[i][j] = 0;
				if (c > 0) {
					coverageMean[i][j] = sum/c;
				}
			}
		}
			
		return coverageMean;
	}
	
	public void updateFeedback(Feedback[] feedback, String type){
		double[][][] coverage =  resource.getCoverageWithFeedback(feedback);
		double[][][][] coverageAux = new double[1][][][];
		coverageAux[0] = coverage;
		if (type.equals("max")){
			updateWithMax(coverageAux);
		} else if (type.equals("mean")){
			updateWithMean(coverageAux);
		}
	}
	
	public void updateSuggestionsFeedback(Feedback[] feedback, String suggestionsFilename, String topicId, String type){
		double[][][] coverageFeedback =  resource.getCoverageWithFeedback(feedback);
		double[][] coverageSuggestions =  resource.getCoverageWithSuggestions(topicId,suggestionsFilename);
		double[][][] coverageAux = new double[2][][];
		coverageAux[0] = coverageSuggestions;
		if (type.equals("max")){
			
			coverageAux[1] = getCoverageWithMax(coverageFeedback);
			update(coverageAux);
		} else if (type.equals("mean")){
			coverageAux[1] = getCoverageWithMean(coverageFeedback);
			update(coverageAux);
		}
	}

	public void updateHSuggestions(String type, String suggestionsFilename) {
		
		double[][][] coverage =  resource.getHierarchicalCoverageWithSuggestions(topicId, suggestionsFilename);
		
		double[][][][] coverageAux = new double[1][][][];
		coverageAux[0] = coverage;
		if (type.equals("max")){
			updateWithMax(coverageAux);
		} else if (type.equals("mean")){
			updateWithMean(coverageAux);
		}
	}
	
	public void updateStoppingRules(Feedback[] feedback){
		resource.updateRules(feedback);
	}

	public void updateSuggestions(String suggestionsFilename) {
		double[][] coverage =  resource.getCoverageWithSuggestions(topicId, suggestionsFilename);
		
		double[][][] coverageAux = new double[1][][];
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
