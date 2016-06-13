package br.ufmg.dcc.latin.searcher;


import java.util.Set;

import br.ufmg.dcc.latin.searcher.models.WeightingModel;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;

public interface Searcher {
	


	public ResultSet getNextResults();

	public void search(String query);
	
	public ResultSet search(String query, Integer max);
	
	public ResultSet search(String query, Set<String> ids);
	
	public ResultSet search(String query, Set<String> ids, WeightingModel weightingModel);
}
