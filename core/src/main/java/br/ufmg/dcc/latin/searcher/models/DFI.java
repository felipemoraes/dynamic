package br.ufmg.dcc.latin.searcher.models;

import java.util.HashMap;

import org.apache.lucene.search.Explanation;
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

	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.models.WeightingModel#computeDetails(org.apache.lucene.search.Explanation)
	 */
	@Override
	public HashMap<String, HashMap<String, Double>> getDetails(Explanation explanation) {
		// TODO Auto-generated method stub
		return null;
	}

}
