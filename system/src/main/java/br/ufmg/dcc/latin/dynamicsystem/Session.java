package br.ufmg.dcc.latin.dynamicsystem;

import java.util.List;

import br.ufmg.dcc.latin.diversity.scoring.Scorer;
import br.ufmg.dcc.latin.diversity.scoring.ScorerFactory;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;

public class Session {
	
	Scorer scorer;
	String scorerName;

	private List<float[]> params;
	InteractiveReranker reranker;
	
	public Session(){
		reranker = new InteractiveReranker();
	}
	
	private String getName(float[] params){
		String name = scorerName;
		for (int i = 0; i < params.length; i++) {
			name += "_" + String.format("%.2f", params[i]);
		}
		return name;
	}

	public void run(String query, String index, String topicId){
		reranker.start(query, index);
		for (float[] param : params) {
			String name = getName(param);
			run(topicId, name, param);
		}
	}
	
	public void run( String topicId, String name, float[] params){
		scorer.build(params);
		reranker.setScorer(scorer);
		reranker.start(params);
		for (int i = 0; i < 10; i++) {
			ResultSet resultSet = reranker.get();
			Evaluator.writeToFile(name, topicId, resultSet, i);
			Feedback[] feedback = TrecUser.get(resultSet, topicId);
			reranker.update(feedback);
		}
	}



	public void setScorer(String scorer) {
		scorerName = scorer;
		this.scorer = ScorerFactory.getInstance(scorer);
	}

	public List<float[]> getParams() {
		return params;
	}

	public void setParams(List<float[]> params) {
		this.params = params;
	}

}
