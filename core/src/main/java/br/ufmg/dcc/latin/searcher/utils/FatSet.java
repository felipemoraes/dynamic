/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;
import java.util.Map.Entry;

/**
 * @author Felipe Moraes
 *
 */
public class FatSet {
	
	private HashMap<String,Double> models;
	private HashMap<String, HashMap<String, Double>> details;
	
	public FatSet(){
		String[] independences = {"standardized", "saturated", "chisquared"};
		String[] distributions  = {"ll", "spl"};
		String[] lambdas = {"df", "ttf"};
		String[] basicModels = {"be", "d", "g", "if", "in", "ine","p"};
		String[] afterEffects = {"no", "b" ,"l"};
		String[] normalizations = {"no", "h1", "h2", "h3", "z"};
		
		setModels(new HashMap<String,Double>());
		setDetails( new HashMap<String, HashMap<String, Double>>());
		getModels().put("BM25", null);
		getModels().put("TFIDF", null);
		getModels().put("LMDirichlet", null);
		getModels().put("LMJelinekMercer", null);
		for (String independence : independences) {
			getModels().put("DFI_" + independence, null);
		}		
		for (String basicModel : basicModels) {
			for (String afterEffect : afterEffects) {
				for (String normalization : normalizations) {
					getModels().put("DFR_" + basicModel + "_" 
							+ afterEffect + "_" + normalization, null);
				}
			}
		}
		
		for (String lambda : lambdas) {
			for (String distribution : distributions) {
				for (String normalization : normalizations) {
					getModels().put("IB_" + distribution + "_" + lambda + "_" + normalization, null);
				}
			}
		}
		
		getModels().put("TF", null);
		getModels().put("IDF", null);
		getModels().put("DL", null);
	}

	public HashMap<String,Double> getModels() {
		return models;
	}

	public void setModels(HashMap<String,Double> models) {
		this.models = models;
	}
	
	public String toString(){
		String str = "";
		for ( Entry<String, Double> model : models.entrySet()) {
			if (model.getValue() != null) {
				str += model.getKey() + ":" + model.getValue().toString() + " ";
			}
		}
		return str;
		
	}

	public HashMap<String, HashMap<String, Double>> getDetails() {
		return details;
	}

	public void setDetails(HashMap<String, HashMap<String, Double>> details) {
		this.details = details;
	}

}
