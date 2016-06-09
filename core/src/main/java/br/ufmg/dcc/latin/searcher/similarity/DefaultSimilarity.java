package br.ufmg.dcc.latin.searcher.similarity;

import org.elasticsearch.common.settings.Settings;

public class DefaultSimilarity implements Similarity {

	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
    		.put("index.similarity.default.type","default")
            .build();
		return settings;
	}

}
