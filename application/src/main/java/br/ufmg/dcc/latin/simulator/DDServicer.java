package br.ufmg.dcc.latin.simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import br.ufmg.dcc.latin.models.HxQuADReRanker;
import br.ufmg.dcc.latin.models.xQuADReRanker;
import br.ufmg.dcc.latin.search.elements.Feedback;

public class DDServicer {
	
	
	private  HxQuADReRanker hReRanker;
	private  xQuADReRanker xReRanker;
	
	private  DDSimulator simulator;
	private String googleSuggestionsFile;
	private String topicsSuggestionsFile;
	
	private  String runId;
	
	DDServicer (String runId) throws FileNotFoundException, IOException{
		this.runId = runId;    	
    	simulator = new DDSimulator("src/main/resources/truth_data_2016.txt");
    	hReRanker = new HxQuADReRanker();
    	xReRanker = new xQuADReRanker();
    	googleSuggestionsFile =  "src/main/resources/GoogleSuggestions.txt";
    	topicsSuggestionsFile = "src/main/resources/topicsSuggestions.txt";
	}
	
	 public void run(String type, String indexName, String query, String topicId, String[] args){
		 	
			if (type.equals("XMaxF")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADFeedback(indexName,query,topicId,ambiguity,depth, "max",stopType);
				
			} else if (type.equals("XMeanF")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADFeedback(indexName,query,topicId,ambiguity,depth,"mean",stopType);
				
			} else if (type.equals("HF")){
				
				 double[] ambiguity = new double[2];
				 ambiguity[0] = Double.parseDouble(args[1]);
				 ambiguity[1] = 0.5;
				 int depth = Integer.parseInt(args[2]);
				 String stopType = args[3];
				 HxQuADFeedback(indexName,query,topicId,ambiguity,depth,stopType);
				 
			} else if (type.equals("XMaxHG")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,googleSuggestionsFile, "H","max",stopType);
				
			} else if (type.equals("XMeanHG")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,googleSuggestionsFile, "H","mean",stopType);
				
			}else if (type.equals("XMaxG")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,googleSuggestionsFile, "","max",stopType);
				
			} else if (type.equals("XMeanG")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,googleSuggestionsFile, "","mean",stopType);
				
			} else if (type.equals("HG")){
				
				 double[] ambiguity = new double[2];
				 ambiguity[0] = Double.parseDouble(args[1]);
				 ambiguity[1] = 0.5;
				 int depth = Integer.parseInt(args[2]);
				 String stopType = args[3];
				 HxQuADSuggestions(indexName,query,topicId,ambiguity,depth,googleSuggestionsFile,stopType);
				 
			} else if (type.equals("XMaxHT")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,topicsSuggestionsFile,"H","max",stopType);
				
			} else if (type.equals("XMeanHT")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,topicsSuggestionsFile, "H", "mean",stopType);
				
			}else if (type.equals("XMaxT")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,topicsSuggestionsFile, "", "max",stopType);
				
			} else if (type.equals("XMeanT")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADSuggestions(indexName,query,topicId,ambiguity,depth,topicsSuggestionsFile,"", "mean",stopType);
				
			} else if (type.equals("HT")){
				
				 double[] ambiguity = new double[2];
				 ambiguity[0] = Double.parseDouble(args[1]);
				 ambiguity[1] = 0.5;
				 int depth = Integer.parseInt(args[2]);
				 String stopType = args[3];
				 HxQuADSuggestions(indexName,query,topicId,ambiguity,depth,topicsSuggestionsFile,stopType);
				 
			}	else if (type.equals("XMaxO")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADOracle(indexName,query,topicId,ambiguity,depth, "max",stopType);
				
			} else if (type.equals("XMeanO")){
				
				double ambiguity = Double.parseDouble(args[1]);
				int depth = Integer.parseInt(args[2]);
				String stopType = args[3];
				xQuADOracle(indexName,query,topicId,ambiguity,depth, "mean",stopType);
				
			} else if (type.equals("HO")){
				
				 double[] ambiguity = new double[2];
				 ambiguity[0] = Double.parseDouble(args[1]);
				 ambiguity[1] = 0.5;
				 int depth = Integer.parseInt(args[2]);
				 String stopType = args[3];
				 HxQuADOracle(indexName,query,topicId,ambiguity,depth,stopType);
				 
			} else if (type.equals("XMaxFS")) {
				double ambiguity = Double.parseDouble(args[1]);
				double mixture = Double.parseDouble(args[2]);
				String stopType = args[3];
				xQuADMulti(indexName,query,topicId,ambiguity,1000,mixture,googleSuggestionsFile, "max",stopType);
			} else if (type.equals("XMeanFS")) {
				double ambiguity = Double.parseDouble(args[1]);
				double mixture = Double.parseDouble(args[2]);
				String stopType = args[3];
				xQuADMulti(indexName,query,topicId,ambiguity,1000,mixture,googleSuggestionsFile, "mean",stopType);
			} else if (type.equals("HFS")) {
				 double[] ambiguity = new double[2];
				 ambiguity[0] = Double.parseDouble(args[1]);
				 ambiguity[1] = 0.5;
				double mixture = Double.parseDouble(args[2]);
				String stopType = args[3];
				HxQuADMulti(indexName,query,topicId,ambiguity,1000,mixture,googleSuggestionsFile,stopType);
			}
			
			else if (type.equals("LM")){
				String stopType = args[1];
				 LM(indexName,query,topicId,stopType);
			}
	    	
	    }

	 
	 	private void xQuADMulti(String indexName, String query, String topicId, double ambiguity,
				int depth,  double mixture, String suggestionsFilename, String type, String stopType){
	 		
			double[] weight = new double[2];
	    	xReRanker.start(indexName, query,topicId, ambiguity,depth,weight,stopType);
	    	weight[0] = 1.0;
	    	weight[1] = 0.0;
	    	xReRanker.updateSuggestionsFeedback(null, suggestionsFilename, topicId, type);
	    	Map<String,Double> response = xReRanker.get();
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		xReRanker.updateSuggestionsFeedback(feedback, suggestionsFilename, topicId, type);
	    		xReRanker.updateStoppingRules(feedback);
	    		weight[0] = mixture;
	    		weight[1] = 1-mixture;
	    		xReRanker.setWeight(weight);
	    		
	    		response = xReRanker.get();
	    	}
	 	}
	 	
	 	private void HxQuADMulti(String indexName, String query, String topicId, double[] ambiguity,
				int depth,  double mixture, String suggestionsFilename,  String stopType){
	 		
			double[] weight = new double[2];
	    	hReRanker.start(indexName, query,topicId, ambiguity,depth,weight,stopType);
	    	weight[0] = 0.0;
	    	weight[1] = 0.0;
	    	hReRanker.updateSuggestionsFeedback(null, suggestionsFilename, topicId);
	    	Map<String,Double> response = hReRanker.get();
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		hReRanker.updateSuggestionsFeedback(feedback, suggestionsFilename, topicId);
	    		hReRanker.updateStoppingRules(feedback);
	    		weight[0] = mixture;
	    		weight[1] = 1-mixture;
	    		hReRanker.setWeight(weight);
	    		
	    		response = hReRanker.get();
	    	}
	 	}

		private void HxQuADSuggestions(String indexName, String query, String topicId, double[] ambiguity,
				int depth, String suggestionsFilename,  String stopType) {
			double[] weight = new double[1];
			weight[0] = 1.0;
	    	hReRanker.start(indexName, query,topicId, ambiguity,depth,weight,stopType);
	    	hReRanker.updateSuggestions(suggestionsFilename);
	    	Map<String,Double> response = hReRanker.get();
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		hReRanker.updateStoppingRules(feedback);
	    		response = hReRanker.get();
	    	}
			
		}

		private void xQuADSuggestions(String indexName, String query, String topicId, double ambiguity,
				int depth,  String suggestionsFilename, String coverageType, String type, String stopType) {
			double[] weight = new double[1];
			weight[0] = 1.0;
	    	xReRanker.start(indexName, query,topicId,ambiguity,depth,weight,stopType);
	    	if (coverageType.equals("H")) {
	    		xReRanker.updateHSuggestions(type,suggestionsFilename);
	    	} else {
	    		xReRanker.updateSuggestions(suggestionsFilename);
	    	}
	    	
	    	Map<String,Double> response = xReRanker.get();
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		xReRanker.updateStoppingRules(feedback);
	    		response = xReRanker.get();
	    	}
		}


		private void LM(String indexName, String query, String topicId, String stopType){
			double[] weight = new double[1];
			weight[0] = 1.0;
	    	xReRanker.start(indexName, query, topicId, 0.0,1000,weight,stopType);
	    	Map<String,Double> response = xReRanker.get();
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		xReRanker.updateStoppingRules(feedback);
	    		response = xReRanker.get();
	    	}
	    }
	    
	    private void HxQuADFeedback(String indexName, String query, String topicId, double[] ambiguity, int depth, String stopType){
			double[] weight = new double[1];
			weight[0] = 1.0;
	    	hReRanker.start(indexName, query,topicId, ambiguity,depth,weight,stopType);
	    	Map<String,Double> response = hReRanker.get();
	    	
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		hReRanker.updateFeedback(feedback);
	    		hReRanker.updateStoppingRules(feedback);
	    		response = hReRanker.get();
	    	}
	    }
	    
	    private void xQuADFeedback(String indexName, String query, String topicId, double ambiguity, int depth, String type, String stopType){
			double[] weight = new double[1];
			weight[0] = 1.0;
	    	xReRanker.start(indexName, query, topicId, ambiguity,depth,weight,stopType);
	    	Map<String,Double> response = xReRanker.get();
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		xReRanker.updateFeedback(feedback, type);
	    		xReRanker.updateStoppingRules(feedback);
	    		response = xReRanker.get();
	    	}
	    }
	    
	    private void HxQuADOracle(String indexName, String query, String topicId, double[] ambiguity, int depth, String stopType){
			double[] weight = new double[1];
			weight[0] = 1.0;
	    	hReRanker.start(indexName, query,topicId, ambiguity,depth,weight,stopType);
	    	double[][][][] coverageAux = new double[1][][][];
			coverageAux[0] = simulator.getCoverage(topicId,hReRanker.getResource().docNos);
	    	hReRanker.update(coverageAux);
	    	Map<String,Double> response = hReRanker.get();
	    	
	    	while (response.size() > 1) {
	    		Feedback[] feedback  = simulator.performStep(runId, topicId, response);
	    		hReRanker.updateStoppingRules(feedback);
	    		response = hReRanker.get();
	    	}
	    }
	    
	    private void xQuADOracle(String indexName, String query, String topicId, double ambiguity, int depth, String type, String stopType){
			double[] weight = new double[1];
			weight[0] = 1.0;
	    	xReRanker.start(indexName, query, topicId, ambiguity,depth,weight,stopType);
	    	double[][][][] coverageAux = new double[1][][][];
	    	
			coverageAux[0] = simulator.getCoverage(topicId,xReRanker.getResource().docNos);
    		if (type.equals("max")) {
    			xReRanker.updateWithMax(coverageAux);
    		} else if (type.equals("mean")) {
    			xReRanker.updateWithMean(coverageAux);
    		}
	    	Map<String,Double> response = xReRanker.get();
	    	while (response.size() > 1) {
	    		Feedback[] feedback = simulator.performStep(runId, topicId, response);
	    		xReRanker.updateStoppingRules(feedback);
	    		response = xReRanker.get();
	    	}
	    }
	   
}
