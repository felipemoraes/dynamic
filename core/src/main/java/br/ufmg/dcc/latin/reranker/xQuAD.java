package br.ufmg.dcc.latin.reranker;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.ResultSet;

public class xQuAD extends InteractiveReranker {

	public xQuAD(FeedbackModeling feedbackModeling) {
		super(feedbackModeling);
	}

	double lambda;
	
	
	public double[] importance;
	public double[] novelty;
	public double[][] coverage;
	

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
	public ResultSet get(){
		
		coverage = feedbackModeling.coverage;
		importance = feedbackModeling.importance;
		novelty = feedbackModeling.novelty;
		updateNovelty();
		
		return super.get();
	}
	
	@Override
	public void start(ResultSet resultSet, double[] params){
		super.start(resultSet, params);
		
		relevance = normalize(relevance);
		lambda = params[0];
		feedbackModeling = feedbackModeling.getInstance(docnos);
		coverage = feedbackModeling.coverage;
		importance = feedbackModeling.importance;
		novelty = feedbackModeling.novelty;
	}
	

	
	@Override
	public void update(int docid){
		for (int i = 0; i < novelty.length; i++) {
			novelty[i] *= (1-coverage[docid][i]);
		}
		
	}

	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		feedbackModeling.update(feedback);
	}
	
	public void updateNovelty(){
		for (int j = 0; j < docids.length; ++j) {
			if (! selected.has(j)) {
				continue;
			}
			update(j);
		}
	}


}
