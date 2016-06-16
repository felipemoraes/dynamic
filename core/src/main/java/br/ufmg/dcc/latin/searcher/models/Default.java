package br.ufmg.dcc.latin.searcher.models;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.utils.Details;

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
	public Details getDetails(Explanation explanation) {
		// TODO Auto-generated method stub
		return null;
	}

}
