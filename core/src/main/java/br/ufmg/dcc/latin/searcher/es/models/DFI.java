package br.ufmg.dcc.latin.searcher.es.models;


import org.elasticsearch.common.settings.Settings;



public class DFI implements WeightingModel {

	private String independenceMeasure;
	
	public DFI() {
		this.independenceMeasure = "standardized";
	}

	public DFI(String independenceMeasure) {
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
