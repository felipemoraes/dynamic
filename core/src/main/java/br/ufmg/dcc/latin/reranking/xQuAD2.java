package br.ufmg.dcc.latin.reranking;

import java.util.Arrays;

import br.ufmg.dcc.latin.controller.FlatAspectController;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xQuAD2 extends InteractiveReranker {
	
	float lambda;
	
	private FlatAspectController aspectControler;
	
	private float[] importance;
	private float[] novelty;
	private float[][] coverage;
	
	int n;
	
	@Override
	public void start(float[] params){
		super.start(params);
		relevance = normalize(relevance);
		n = relevance.length;
		aspectControler = new FlatAspectController();
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		lambda = params[1];
		novelty = new float[relevance.length];
		Arrays.fill(novelty, 1.0f);
	}
	

	@Override
	protected float score(int docid) {
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i];
		}
		
		float score = (1-lambda)*relevance[docid] + lambda*diversity*novelty[docid];
		return score;
	}


	@Override
	public void update(int docid) {
		
		float[] probs = new float[n];
	    Arrays.fill(probs, 0);

	    for(int i = 0;i<probs.length;++i) {
	    	probs[i] = cosine(coverage[i],coverage[docid]);
	    }
	    
	    probs = normalize(probs);
	    
	    novelty[docid] *= (1-probs[docid]);
		
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
	public void update(Feedback[] feedback) {
		aspectControler.miningDiversityAspects(feedback);
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		novelty = new float[n];
		Arrays.fill(novelty, 1f);
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
		return "";
	}

}