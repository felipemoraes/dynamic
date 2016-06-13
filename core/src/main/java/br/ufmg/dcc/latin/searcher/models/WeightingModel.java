package br.ufmg.dcc.latin.searcher.models;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.utils.PropertyDetails;

public interface WeightingModel {
	public Settings getSettings();
	public PropertyDetails getDetails(Explanation explanation);

}
