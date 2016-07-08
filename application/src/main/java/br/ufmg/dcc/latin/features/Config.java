package br.ufmg.dcc.latin.features;

import br.ufmg.dcc.latin.searcher.es.models.WeightingModel;

public final class Config {
	
	public static String QUERY_INDEPENDENT_FILENAME;
	public static String QUERY_DEPENDENT_FILENAME;
	public static WeightingModel INITIAL_RANKING_MODEL;
	public static String ES_INDEX_NAME;
	public static String ES_DOC_TYPE;
	public static String[] ES_FIELDS;
	public static String OUTPUT_FILENAME;

}
