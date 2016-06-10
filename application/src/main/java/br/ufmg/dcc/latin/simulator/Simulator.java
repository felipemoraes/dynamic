package br.ufmg.dcc.latin.simulator;

import br.ufmg.dcc.latin.searcher.utils.FeedbackSignals;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;

public interface Simulator {
	
	public FeedbackSignals performStep(String runId, String topicId, ResultSet resultSet);

}
