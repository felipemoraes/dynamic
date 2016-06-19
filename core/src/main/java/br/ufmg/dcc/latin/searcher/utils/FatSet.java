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
	
	private HashMap<String,Float> models;
	
	
	public FatSet(){
		String[] independences = {"standardized", "saturated", "chisquared"};
		String[] distributions  = {"ll", "spl"};
		String[] lambdas = {"df", "ttf"};
		String[] basicModels = {"be", "d", "g", "if", "in", "ine","p"};
		String[] afterEffects = {"no", "b" ,"l"};
		String[] normalizations = {"no", "h1", "h2", "h3", "z"};
		
		models = new HashMap<String,Float>();
		
		models.put("BM25", 0F);
		models.put("TFIDF", 0F);
		models.put("LMDirichlet", 0F);
		models.put("LMJelinekMercer", 0F);
		for (String independence : independences) {
			models.put("DFI_" + independence, 0F);
		}		
		for (String basicModel : basicModels) {
			for (String afterEffect : afterEffects) {
				for (String normalization : normalizations) {
					models.put("DFR_" + basicModel + "_" 
							+ afterEffect + "_" + normalization, 0F);
				}
			}
		}
		
		for (String lambda : lambdas) {
			for (String distribution : distributions) {
				for (String normalization : normalizations) {
					models.put("IB_" + distribution + "_" + lambda + "_" + normalization, 0F);
				}
			}
		}
		
		models.put("TF", 0F);
		models.put("IDF", 0F);
		models.put("DL", 0F);
	}


	
	public String toString(){
		String str = "";
		for ( Entry<String, Float> model : models.entrySet()) {
			if (model.getValue() != null) {
				str += model.getKey() + ":" + model.getValue().toString() + " ";
			}
		}
		return str;
		
	}



	public HashMap<String,Float> getModels() {
		return models;
	}



	public void setModels(HashMap<String,Float> models) {
		this.models = models;
	}


}
