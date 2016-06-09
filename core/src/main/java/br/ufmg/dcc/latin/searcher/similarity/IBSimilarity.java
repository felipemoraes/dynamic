package br.ufmg.dcc.latin.searcher.similarity;

import org.elasticsearch.common.settings.Settings;

public class IBSimilarity implements Similarity {

	private String distribution;
	private String lambda;
	private String normalization;
	
	
	public IBSimilarity() {
		
	}
	public IBSimilarity(String distribution, String lambda, String normalization) {
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
