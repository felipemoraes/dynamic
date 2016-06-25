package br.ufmg.dcc.latin.searcher.es.models;


import org.elasticsearch.common.settings.Settings;



public class LMJelinekMercer implements WeightingModel {

	private Double lambda;

	public LMJelinekMercer(){
		this.lambda = 0.1;
	}
	
	public LMJelinekMercer(Double lambda){
		this.lambda = lambda;
	}
	
	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","LMJelinekMercer")
            .put("index.similarity.default.lambda",this.lambda)
            .build();
		return settings;
	}


}
