package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.querying.ResultSet;

public interface Reranker {
	public ResultSet reranking(ResultSet baselineResultSet);
	
	default float[] normalize(float[] values){
		float sum = 0;
		for (int i = 0; i < values.length; i++) {
			sum += values[i];
		}
		for (int i = 0; i < values.length; i++) {
			if (sum > 0) {
				values[i] = values[i]/sum;
			}
		}
		return values;
	}
}
