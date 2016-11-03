package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;

public abstract class InteractiveReranker implements Reranker {
	
	
	protected SelectedSet selected;

	@Override
	public ResultSet reranking(ResultSet baselineResultSet) {
		return null;
		
	}

}