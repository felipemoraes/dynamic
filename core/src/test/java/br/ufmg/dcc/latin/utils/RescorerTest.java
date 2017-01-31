package br.ufmg.dcc.latin.utils;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.baselineranker.AdHocBaselineRanker;
import br.ufmg.dcc.latin.baselineranker.BaselineRanker;
import br.ufmg.dcc.latin.user.UserQuery;

public class RescorerTest {

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		
		UserQuery userQuery = new UserQuery();
		userQuery.index = "ebola16";
		userQuery.query = "ebola";
		
		BaselineRanker baselineRanker = new AdHocBaselineRanker("DPH", new double[]{0.15,0.85});
		assertNotEquals(baselineRanker.getResultList(userQuery), null );
		assertNotEquals(SharedCache.scores, null );
		double[] scores = RescorerSystem.rescore("ebola");
		Assert.assertEquals(scores[0], SharedCache.scores[0], 0.000001);
		System.out.println(scores[0] + " " +  SharedCache.scores[0]);
	}

}
