package br.ufmg.dcc.latin.reranking;


import java.util.Arrays;

import br.ufmg.dcc.latin.controller.PassageAspectMining;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xQuAD3 extends InteractiveReranker {

	float lambda;
	
	private PassageAspectMining aspectControler;
	
	public float[] importance;
	public float[][] novelty;
	public float[][] coverage;
	public float[][][] features;
	
	int n;
	@Override
	public float score(int docid){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i]*novelty[i][docid];
		}
		
		float score = (1-lambda)*relevance[docid] + lambda*diversity;
		
		return score;
	}
	
	@Override
	public void start(float[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		n = relevance.length;
		aspectControler = new PassageAspectMining();
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		novelty = new float[0][n];
		features = new float[n][0][0];
	}

	@Override
	protected void update(int docid) {
		for (int i = 0; i < novelty.length; i++) {
			float[] probs = new float[n];
			
			for (int j = 0; j < probs.length; j++) {
				probs[j] = cosine(features[docid][i], features[j][i]);
			}
			probs = normalize(probs);
			for (int j = 0; j < probs.length; j++) {
				novelty[i][j] *= (1-probs[j]);
			}
		}
		
	}

	@Override
	public void update(Feedback[] feedback) {
		
		aspectControler.miningDiversityAspects(feedback);
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		features = aspectControler.features;
		int aspectSize = importance.length;
		novelty = new float[aspectSize][n];
		for (int i = 0; i < novelty.length; i++) {
			Arrays.fill(novelty[i], 1f);
		}
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
	
	private float cosine(float[] v1, float[] v2){
		float denom = 0;
		float sum1 = 0;
		float sum2  = 0;
		
	
		for (int i = 0; i < v2.length; i++) {
			denom += v1[i]*v2[i];
		}
		
		for (int i = 0; i < v2.length; i++) {
			sum1 += v1[i]*v1[i];
			sum2 += v2[i]*v2[i];
		}
		sum1 = (float) Math.sqrt(sum1);
		sum2 = (float) Math.sqrt(sum2);
		
		if (sum1*sum2 > 0){
			return denom/(sum1*sum2);
		} 
		
		return 0;
	}

	@Override
	public String debug(String topicid, int iteration) {
		return "";
	}


}
