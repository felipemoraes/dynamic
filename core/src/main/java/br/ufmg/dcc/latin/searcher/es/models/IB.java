package br.ufmg.dcc.latin.searcher.es.models;

import org.elasticsearch.common.settings.Settings;

public class IB implements WeightingModel {

	private String distribution;
	private String lambda;
	private String normalization;
	
	
	public IB() {
		this.distribution = "ll";
		this.lambda = "df";
		this.normalization = "no";
	}
	public IB(String distribution, String lambda, String normalization) {
		this.distribution = distribution;
		this.lambda = lambda;
		this.normalization = normalization;
	}
	

	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","IB")
            .put("index.similarity.default.distribution",this.distribution)
            .put("index.similarity.default.lambda",this.lambda)
            .put("index.similarity.default.normalization",this.normalization)
            .build();
		return settings;
	}

}
