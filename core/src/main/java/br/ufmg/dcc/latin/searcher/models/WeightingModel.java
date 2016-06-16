package br.ufmg.dcc.latin.searcher.models;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.utils.Details;

public interface WeightingModel {
	public Settings getSettings();
	public Details getDetails(Explanation explanation);

}
