package br.ufmg.dcc.latin.baselineranker;

import br.ufmg.dcc.latin.user.UserQuery;

public abstract class BaselineRanker {
	public abstract ResultList getResultList(UserQuery userQuery);
}
