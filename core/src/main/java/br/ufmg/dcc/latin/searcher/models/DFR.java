package br.ufmg.dcc.latin.searcher.models;

import java.util.HashMap;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

public class DFR implements WeightingModel{

	public DFR(){
		
	}
	private String basicModel;
	private String afterEffect;
	private String normalization;
	public DFR(String basicModel, String afterEffect, String normalization){
		this.basicModel = basicModel;
		this.afterEffect = afterEffect;
		this.normalization  = normalization;
	}
	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","DFR")
            .put("index.similarity.default.basic_model",this.basicModel)
            .put("index.similarity.default.after_effect",this.afterEffect)
            .put("index.similarity.default.normalization", this.normalization)
            .build();
		return settings;
	}
	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.models.WeightingModel#computeDetails(org.apache.lucene.search.Explanation)
	 */
	@Override
	public HashMap<String, HashMap<String, Double>> getDetails(Explanation explanation) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
