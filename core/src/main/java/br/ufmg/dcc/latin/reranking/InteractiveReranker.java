package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.querying.QueryResultSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.querying.SelectedSet;

public abstract class InteractiveReranker implements Reranker {
	
	
	private SelectedSet selected;

	@Override
	public ResultSet reranking(ResultSet baselineResultSet) {
		return null;
	}
	
	public ResultSet getTopResults(ResultSet resultSet){
		int[] docids = new int[5];
		String[] docnos = new String[5];
		float[] scores = new float[5];
		int n = resultSet.getDocids().length;
		
		if (selected == null) {
			selected = new SelectedSet();
		}
		for (int i = 0; i < 5; ++i){
			int maxRank = -1;
			float maxScore = -1;
			
			for (int j = 0; j < n; j++) {
				if (selected.has(resultSet.getDocids()[j])){
					continue;
				}
				if (maxScore < resultSet.getScores()[j]) {
					maxRank = j;
					maxScore = resultSet.getScores()[j];
				}
			}
			docids[i] = resultSet.getDocids()[maxRank];
			docnos[i] = resultSet.getDocnos()[maxRank];
			scores[i] = resultSet.getScores()[maxRank];
			
			selected.put(docids[i]);
			
		}
		ResultSet topResultSet = new QueryResultSet();
		topResultSet.setDocids(docids);
		topResultSet.setDocnos(docnos);
		topResultSet.setScores(scores);
		
		return topResultSet;
	}

	public SelectedSet getSelected() {
		return selected;
	}

	public void setSelected(SelectedSet selected) {
		this.selected = selected;
	}

}