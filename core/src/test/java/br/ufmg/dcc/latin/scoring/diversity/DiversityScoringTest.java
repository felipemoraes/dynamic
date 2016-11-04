package br.ufmg.dcc.latin.scoring.diversity;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.diversity.Aspect;
import br.ufmg.dcc.latin.diversity.FlatAspect;


public class DiversityScoringTest {

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

		Aspect[][] coverage = new Aspect[5][2];
		Aspect[] importance = new Aspect[2];
		Aspect[] novelty = new Aspect[2];
		importance[0] = new FlatAspect(0.6f);
		importance[1]= new FlatAspect(0.4f);
		novelty[0] = new FlatAspect(1.0f);
		novelty[1] = new FlatAspect(1.0f);

		coverage[0][0] = new FlatAspect(0.3f);
		coverage[1][0] = new FlatAspect(0.7f);
		coverage[2][0] = new FlatAspect(0.2f);
		coverage[3][0] = new FlatAspect(0.7f);
		coverage[4][0] = new FlatAspect(0.4f);
		
		coverage[0][1] = new FlatAspect(0.4f);
		coverage[1][1] = new FlatAspect(0.6f);
		coverage[2][1] = new FlatAspect(0.3f);
		coverage[3][1] = new FlatAspect(0.8f);
		coverage[4][1] = new FlatAspect(0.2f);
		
		xQuAD divX = new xQuAD(importance,coverage,novelty);
		assertEquals(0.52, 0.35+ divX.div(0)/2, 0.00001);
		assertEquals(0.58, 0.25+ divX.div(1)/2, 0.00001);
		assertEquals(0.27, 0.15+ divX.div(2)/2, 0.00001);
		assertEquals(0.47, 0.10+ divX.div(3)/2, 0.00001);
		assertEquals(0.21, 0.05+ divX.div(4)/2, 0.00001);
		
		divX.update(1);
		assertEquals(0.41, 0.35+ divX.div(0)/2, 0.001);
	}

}
