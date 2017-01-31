package br.ufmg.dcc.latin.user;

import br.ufmg.dcc.latin.baselineranker.ResultList;

public abstract class User {
	public abstract FeedbackList getFeedbackSet(String topicId, ResultList resultList);
}
