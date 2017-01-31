package br.ufmg.dcc.latin.baselineranker;

import static org.junit.Assert.assertNotEquals;

import org.junit.Assert;
import org.junit.Test;

import br.ufmg.dcc.latin.user.UserQuery;
import br.ufmg.dcc.latin.utils.RescorerSystem;
import br.ufmg.dcc.latin.utils.SharedCache;

public class AdHocBaselineRankerTest {


	@Test
	public void test() {
		UserQuery userQuery = new UserQuery();
		userQuery.index = "ebola16";
		userQuery.query = "The ebola songs";
		
		
		BaselineRanker baselineRanker = new AdHocBaselineRanker("DPH", new double[]{0.15,0.85});
		assertNotEquals(baselineRanker.getResultList(userQuery), null );
		
	}

}
