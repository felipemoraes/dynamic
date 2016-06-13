package br.ufmg.dcc.latin.searcher.models;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.utils.PropertyDetails;

public class IB implements WeightingModel {

	private String distribution;
	private String lambda;
	private String normalization;
	
	
	public IB() {
		
	}
	public IB(String distribution, String lambda, String normalization) {
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
	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.models.WeightingModel#computeDetails(org.apache.lucene.search.Explanation)
	 */
	@Override
	public PropertyDetails getDetails(Explanation explanation) {
		// TODO Auto-generated method stub
		return null;
	}

}
