package br.ufmg.dcc.latin.dynamicsystem;

import java.util.List;

import javax.swing.DebugGraphics;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;
import br.ufmg.dcc.latin.reranking.InteractiveRerankerFactory;

public class Session {
	
	
	String rerankerName;

	private List<float[]> params;
	InteractiveReranker reranker;
	
	public Session(){
	}
	
	private String getName(float[] params){
		String name = rerankerName;
		for (int i = 0; i < params.length; i++) {
			name += "_" + String.format("%.2f", params[i]);
		}
		return name;
	}

	public void run(String index, String topicId, String query){
		
		reranker.start(query, index);
		
		for (float[] param : params) {
			String name = getName(param);
			run(topicId, name, param);
		}
	}
	
	public void run( String topicId, String name, float[] params){
		reranker.start(params);
		for (int i = 0; i < 10; i++) {
			ResultSet resultSet = reranker.get();
			//Evaluator.writeToFile(name, topicId, resultSet, i);
			Feedback[] feedback = TrecUser.get(resultSet, topicId);
			reranker.update(feedback);
			Evaluator.writeToFile(name, reranker.debug(topicId, i));
		}
	}

	public void setReranker(String reranker) {
		rerankerName = reranker;
		this.reranker = InteractiveRerankerFactory.getInstance(reranker);
	}

	public List<float[]> getParams() {
		return params;
	}

	public void setParams(List<float[]> params) {
		this.params = params;
	}

}
