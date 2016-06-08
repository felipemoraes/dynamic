package br.ufmg.dcc.latin.searcher.similarity;

import org.elasticsearch.common.settings.Settings;

public class LMDirichletSimilarity implements Similarity {

	private Double mu;
	
	public LMDirichletSimilarity(){
		this.mu = 2000.0;
	}
	
	public LMDirichletSimilarity(Double mu){
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
