package br.ufmg.dcc.latin.searcher.similarity;

import org.elasticsearch.common.settings.Settings;

public class DFISimilarity implements Similarity {

	private String independenceMeasure;
	
	public DFISimilarity() {
		this.independenceMeasure = "standardized";
	}

	public DFISimilarity(String independenceMeasure) {
		this.independenceMeasure = independenceMeasure;
	}
	
	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","DFI")
            .put("index.similarity.default.independence_measure",this.independenceMeasure)
            .build();
		return settings;
	}

}
