package br.ufmg.dcc.latin.searcher.es.models;

import org.elasticsearch.common.settings.Settings;


public class DFR implements WeightingModel{


	private String basicModel;
	private String afterEffect;
	private String normalization;
	private String c;
	public DFR(String basicModel, String afterEffect, String normalization, String c){
		this.basicModel = basicModel;
		this.afterEffect = afterEffect;
		this.normalization  = normalization;
		this.c = c;
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
            .put("index.similarity.default.normalization." + this.normalization, c)
            .put("script.inline", true)
            .build();
		return settings;
	}

	public String getName(){
		return "dfr_"+ this.basicModel+"_" + this.afterEffect + "_" + this.normalization;
	}

}
