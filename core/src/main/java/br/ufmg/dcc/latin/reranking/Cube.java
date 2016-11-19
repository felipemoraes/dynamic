package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.controller.FlatAspectController;
import br.ufmg.dcc.latin.feedback.Feedback;

public class Cube extends InteractiveReranker {
	float gamma;
	private static float MaxHeight = 10.0f;
	public float[] importance;
	public float[] novelty;
	public float[][] coverage;
	public float[] accumalatedRelevance;
	
	private FlatAspectController aspectControler;
	
	@Override
	public String debug(String topicid, int iteration) {
		return null;
	}

	@Override
	public void start(float[] params){
		super.start(params);
		gamma = params[1];
		aspectControler = new FlatAspectController();
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		novelty = aspectControler.novelty;
		accumalatedRelevance = aspectControler.accumulatedRelevance;
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
		aspectControler.miningCTAspects(feedback);
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		novelty = aspectControler.novelty;
		accumalatedRelevance = aspectControler.accumulatedRelevance;
	}

}
