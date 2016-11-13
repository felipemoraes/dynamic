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
	
	default float[] scaling(float[] scores){
		float min = Float.POSITIVE_INFINITY;
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] < min) {
				min = scores[i];
			}
		}
		
		float max = Float.NEGATIVE_INFINITY;
		for (int i = 0; i < scores.length; i++) {
			if (scores[i] > max) {
				max = scores[i];
			}
		}
		
		for (int i = 0; i < scores.length; i++) {
			if (max!=min) {
				scores[i] = (scores[i]-min)/(max-min);
			} else {
				scores[i] = 0;
			}
			
		}
		return scores;
		
	}
}
