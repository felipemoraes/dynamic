package br.ufmg.dcc.latin.searcher.similarity;

import org.elasticsearch.common.settings.Settings;

public class LMJelinekMercerSimilarity implements Similarity {

	private Double lambda;

	public LMJelinekMercerSimilarity(){
		this.lambda = 0.1;
	}
	
	public LMJelinekMercerSimilarity(Double lambda){
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
