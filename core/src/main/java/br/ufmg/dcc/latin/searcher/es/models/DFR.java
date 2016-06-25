package br.ufmg.dcc.latin.searcher.es.models;

import org.elasticsearch.common.settings.Settings;


public class DFR implements WeightingModel{


	private String basicModel;
	private String afterEffect;
	private String normalization;
	public DFR(String basicModel, String afterEffect, String normalization){
		this.basicModel = basicModel;
		this.afterEffect = afterEffect;
		this.normalization  = normalization;
	}
	
	public DFR(){
		this.basicModel = "be" ;
		this.afterEffect = "b";
		this.normalization  = "h1";
	}
	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","DFR")
            .put("index.similarity.default.basic_model",this.basicModel)
            .put("index.similarity.default.after_effect",this.afterEffect)
            .put("index.similarity.default.normalization", this.normalization)
            .put("script.inline", true)
            .build();
		return settings;
	}


}
