package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.aspect.AspectMining;
import br.ufmg.dcc.latin.aspect.AspectMiningFactory;
import br.ufmg.dcc.latin.feedback.Feedback;

public class Cube extends InteractiveReranker {
	double gamma;
	private static double MaxHeight = 5.0f;
	private double[] importance;
	private double[] novelty;
	private double[][] coverage;
	private double[] accumalatedRelevance;
	
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
	public void start(double[] params){
		super.start(params);
		gamma = params[1];
		aspectMining = AspectMiningFactory.getInstance(aspectMiningClassName, indexName);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
		accumalatedRelevance = aspectMining.getAccumulatedRelevance();
	}
	
	private float discountFactor(int subtopic){
		
		return (float) Math.pow(gamma, novelty[subtopic]);
	}
	
	@Override
	protected double score(int docid) {
		if (importance.length == 0) {
			return relevance[docid];
		}
		double score = 0;
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
		super.update(feedback);
		aspectMining.miningFeedbackForCube(query,indexName,feedback);
		coverage = aspectMining.getCoverage();
		importance = aspectMining.getImportance();
		novelty = aspectMining.getNovelty();
		accumalatedRelevance = aspectMining.getAccumulatedRelevance();
	}

}
