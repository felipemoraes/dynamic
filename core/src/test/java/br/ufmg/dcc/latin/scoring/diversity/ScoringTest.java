package br.ufmg.dcc.latin.scoring.diversity;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.reranking.xQuAD;



public class ScoringTest {

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
	
	/*
	 * Test case for xQuAD based on the example of Rodrygo Santos on his thesis
	 */

	@Test
	public void xQuADScoreTest() {
		/*
		float[][] coverage = new float[5][2];
		float[] importance = new float[2];
		float[] novelty = new float[2];
		importance[0] = 0.6f;
		importance[1]= 0.4f;
		novelty[0] = 1.0f;
		novelty[1] = 1.0f;

		coverage[0][0] = 0.3f;
		coverage[1][0] = 0.7f;
		coverage[2][0] = 0.2f;
		coverage[3][0] = 0.7f;
		coverage[4][0] = 0.4f;
		
		coverage[0][1] = 0.4f;
		coverage[1][1] = 0.6f;
		coverage[2][1] = 0.3f;
		coverage[3][1] = 0.8f;
		coverage[4][1] = 0.2f;
		float[] relevance = {0.70f,0.50f,0.30f,0.20f,0.10f};
		
		RetrievalCache.scores = relevance;
		AspectCache.coverage = coverage;
		AspectCache.importance = importance;
		AspectCache.novelty = novelty;
		
		xQuAD divX = new xQuAD();
		float[] params = {5f,0.5f};
		divX.build(params);
		
		assertEquals(0.52,divX.score(0), 0.00001);
		assertEquals(0.58, divX.score(1), 0.00001);
		assertEquals(0.27, divX.score(2), 0.00001);
		assertEquals(0.47, divX.score(3), 0.00001);
		assertEquals(0.21, divX.score(4), 0.00001);
		
		divX.update(1);
		assertEquals(0.41,divX.score(0), 0.001);*/
	}

}
