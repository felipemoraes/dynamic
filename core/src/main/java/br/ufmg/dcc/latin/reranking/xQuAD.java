package br.ufmg.dcc.latin.reranking;

import br.ufmg.dcc.latin.controller.FlatAspectController;
import br.ufmg.dcc.latin.controller.FlatAspectGTController;
import br.ufmg.dcc.latin.feedback.Feedback;

public class xQuAD extends InteractiveReranker {

	float lambda;
	
	private FlatAspectGTController aspectControler;
	
	public float[] importance;
	public float[] novelty;
	public float[][] coverage;
	
	
	public xQuAD(){
	}
	

	@Override
	public float score(int docid){
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i]*novelty[i];
		}
		
		float score = (1-lambda)*relevance[docid] + lambda*diversity;
		
		return score;
	}
	
	
	@Override
	public void start(float[] params){
		super.start(params);
		relevance = normalize(relevance);
		lambda = params[1];
		aspectControler = new FlatAspectGTController();
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		novelty = aspectControler.novelty;
	}
	
	
	@Override
	public void update(int docid){
		for (int i = 0; i < novelty.length; i++) {
			novelty[i] *= (1-coverage[docid][i]);
		}
		
	}

	@Override
	public void update(Feedback[] feedback) {
		aspectControler.miningDiversityAspects(feedback);
		coverage = aspectControler.coverage;
		importance = aspectControler.importance;
		novelty = aspectControler.novelty;
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
