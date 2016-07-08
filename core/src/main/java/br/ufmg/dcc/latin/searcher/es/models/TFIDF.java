package br.ufmg.dcc.latin.searcher.es.models;

import org.elasticsearch.common.settings.Settings;



public class TFIDF implements WeightingModel {

	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
    		.put("index.similarity.default.type","default")
    		.put("index.similarity.default.discount_overlaps", false)
            .build();
		return settings;
	}

}
