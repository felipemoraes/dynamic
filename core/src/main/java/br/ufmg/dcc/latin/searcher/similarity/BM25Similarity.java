package br.ufmg.dcc.latin.searcher.similarity;

import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.similarity.Similarity;

public class BM25Similarity implements Similarity {	
	
	private Double b;
	private Double k1;
	
	public BM25Similarity(Double b, Double k1) {
		this.b = b;
		this.k1 = k1;
	}
	
	public BM25Similarity() {
		this.b = 2.0;
		this.k1 = 1.0;
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
