package br.ufmg.dcc.latin.dynamicsystem;

import java.util.List;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranking.InteractiveReranker;
import br.ufmg.dcc.latin.reranking.InteractiveRerankerFactory;

public class Session {
	
	
	String rerankerName;

	private List<double[]> params;
	InteractiveReranker reranker;
	
	public Session(){
	}
	
	private String getName(double[] params){
		String[] rerankerSplit = rerankerName.split(" ");
		String name = "";
		if (rerankerSplit.length > 1) {
			name = rerankerSplit[0] + "_" + rerankerSplit[1].charAt(0);
		} else {
			name = rerankerName;
		}
		
		for (int i = 0; i < params.length; i++) {
			name += "_" + String.format("%.2f", params[i]);
		}
		return name;
	}

	public void run(String index, String topicId, String query){
		reranker.start(query, index);
		for (double[] param : params) {
			String name = getName(param);
			run(topicId, name, param);
		}
	}
	
	public void run( String topicId, String name, double[] params){
		reranker.start(params);
		for (int i = 0; i < 10; i++) {
			ResultSet resultSet = reranker.get();
			Evaluator.writeToFile(name, topicId, resultSet, i);
			Feedback[] feedback = TrecUser.get(resultSet, topicId);
			reranker.update(feedback);
			//Evaluator.writeToFile(name, reranker.debug(topicId, i));
		}
	}

	public void setReranker(String reranker) {
		rerankerName = reranker;
		String[] rerankerSplit = reranker.split(" ");
		if (rerankerSplit.length > 1){ 
			this.reranker = InteractiveRerankerFactory.getInstance(rerankerSplit[0],rerankerSplit[1]);
		} else {
			this.reranker = InteractiveRerankerFactory.getInstance(rerankerName,"");
		}
		
	}

	public List<double[]> getParams() {
		return params;
	}

	public void setParams(List<double[]> params) {
		this.params = params;
	}

}