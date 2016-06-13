package br.ufmg.dcc.latin.searcher.models;

import java.util.HashMap;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

public class Default implements WeightingModel {

	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
    		.put("index.similarity.default.type","default")
            .build();
		return settings;
	}

	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.models.WeightingModel#computeDetails(org.apache.lucene.search.Explanation)
	 */
	@Override
	public HashMap<String, HashMap<String, Double>> getDetails(Explanation explanation) {
		// TODO Auto-generated method stub
		return null;
	}

}
