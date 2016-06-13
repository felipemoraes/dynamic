package br.ufmg.dcc.latin.searcher.models;

import java.util.HashMap;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

public interface WeightingModel {
	public Settings getSettings();
	public HashMap<String, HashMap<String, Double>> getDetails(Explanation explanation);

}
