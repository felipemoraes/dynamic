package br.ufmg.dcc.latin.reranking;

public class xQuADStar extends xQuAD {
	
	public xQuADStar(String aspectMiningClassName) {
		super(aspectMiningClassName);
	}

	@Override
	public double score(int docid){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i];
		}
		
		double score = (1-lambda)*relevance[docid] + lambda*diversity;
		
		return score;
	}

}
