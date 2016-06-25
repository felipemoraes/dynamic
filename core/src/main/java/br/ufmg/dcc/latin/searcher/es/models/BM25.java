package br.ufmg.dcc.latin.searcher.es.models;

import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.es.models.WeightingModel;

public class BM25 implements WeightingModel {	
	
	private Double b;
	private Double k1;
	
	public BM25(Double b, Double k1) {
		this.b = b;
		this.k1 = k1;
	}
	
	public BM25() {
		this.b = 0.75;
		this.k1 = 1.2;
	}

	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","BM25")
            .put("index.similarity.default.b",this.b)
            .put("index.similarity.default.k1",this.k1)
            .build();
		return settings;
	}
}
