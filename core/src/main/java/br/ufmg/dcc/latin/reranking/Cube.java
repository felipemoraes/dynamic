package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;

public class Cube extends InteractiveReranker {
	float gamma;
	private static float MaxHeight = 10.0f;
	private float[] importance;
	private float[] novelty;
	private float[][] coverage;
	private float[] accumalatedRelevance;
	
	private String aspectMiningClassName;
	
	private AspectMining aspectMining;
	
	public Cube(String aspectMiningClassName){
		this.aspectMiningClassName = aspectMiningClassName;
	}
	
	
	@Override
	public String debug(String topicid, int iteration) {
		return null;
	}

	@Override
	public void start(float[] params){
		super.start(params);
		gamma = params[1];
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
		accumalatedRelevance = aspectMining.getAccumulatedRelevance();
	}
	
	private float discountFactor(int subtopic){
		
		return (float) Math.pow(gamma, novelty[subtopic]);
	}
	
	@Override
	protected float score(int docid) {
		if (importance.length == 0) {
			return relevance[docid];
		}
		float score = 0;
		for (int i = 0; i < importance.length; i++) {
			
			if (accumalatedRelevance[i] < MaxHeight) {
				score += discountFactor(i)*importance[i]*coverage[docid][i]*accumalatedRelevance[i];
			}
		}
		return score;
	}

	@Override
	protected void update(int docid) {
		for (int i = 0; i < importance.length; i++) {
			accumalatedRelevance[i] += coverage[docid][i];
		}

	}

	@Override
	public void update(Feedback[] feedback) {
		aspectMining.miningFeedbackForCube(query,indexName,feedback);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
		accumalatedRelevance = aspectMining.getAccumulatedRelevance();
	}

}
