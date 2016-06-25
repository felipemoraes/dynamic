package br.ufmg.dcc.latin.searcher.es.models;

import org.elasticsearch.common.settings.Settings;


public class LMDirichlet implements WeightingModel {

	private Double mu;
	
	public LMDirichlet(){
		this.mu = 2000.0;
	}
	
	public LMDirichlet(Double mu){
		this.mu = mu;
	}
	
	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","LMDirichlet")
            .put("index.similarity.default.mu",this.mu)
            .build();
		return settings;
	}


}
