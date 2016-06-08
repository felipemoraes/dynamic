package br.ufmg.dcc.latin.searcher.similarity;

import org.elasticsearch.common.settings.Settings;

public class DFRSimilarity implements Similarity{

	public DFRSimilarity(){
		
	}
	private String basicModel;
	private String afterEffect;
	private String normalization;
	public DFRSimilarity(String basicModel, String afterEffect, String normalization){
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

	

}
