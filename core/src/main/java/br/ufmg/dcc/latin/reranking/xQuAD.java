package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xQuAD extends InteractiveReranker {

	float lambda;
	
	private AspectMining aspectMining;
	
	public double[] importance;
	public double[] novelty;
	public double[][] coverage;
	
	private String aspectMiningClassName;
	
	public xQuAD(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	

	@Override
	public double score(int docid){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i]*novelty[i];
		}
		
		double score = (1-lambda)*relevance[docid] + lambda*diversity;
		
		return score;
	}
	
	
	@Override
	public void start(float[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName,(int) params[2]);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
	}
	
	
	@Override
	public void update(int docid){
		for (int i = 0; i < novelty.length; i++) {
			novelty[i] *= (1-coverage[docid][i]);
		}
		
	}

	@Override
	public void update(Feedback[] feedback) {
		aspectMining.miningFeedback(indexName, query,feedback);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
		updateNovelty();
	}
	
	public void updateNovelty(){
		for (int j = 0; j < docids.length; ++j) {
			if (! selected.has(docids[j])) {
				continue;
			}
			update(j);
		}
	}


	@Override
	public String debug(String topicid, int iteration) {
		String result = topicid + " " + iteration + " " + coverage.length + " " + coverage[0].length + " ";
		
		for (int i = 0; i < coverage.length; i++) {
			for (int j = 0; j < coverage[i].length; j++) {
				result += coverage[i][j] + " ";
			}
		}
		return result;
	}

}
