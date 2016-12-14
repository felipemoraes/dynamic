package br.ufmg.dcc.latin.retrieval;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.querying.ResultSet;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class RetrievalAndRescoringTest {

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
		ResultSet resultSet = RetrievalController.search("DD16-1", "US Military Crisis Response", "ebola16");
		RetrievalCache.indexName = "ebola16";
		RetrievalController.initRestricted("DD16-1");
		
		for (int i = 0; i < 5; i++) {
			System.out.println(resultSet.docnos[i] + " " + resultSet.scores[i] );
		}
		System.out.println();
		TIntDoubleHashMap complexQuery = ReScorerController.getComplexQuery("US Military Crisis Response");
		double[] scores = ReScorerController.rescore(complexQuery);
		for (int i = 0; i < 5; i++) {
			System.out.println(resultSet.docnos[i] + " " + scores[i] );
		}
		
	}

}
