package br.ufmg.dcc.latin.simulator;

import java.util.Map;

import br.ufmg.dcc.latin.simulator.FeedbackSignals;

public interface Simulator {
	
	public FeedbackSignals performStep(String runId, String topicId, Map<String,Float> resultSet);

}
