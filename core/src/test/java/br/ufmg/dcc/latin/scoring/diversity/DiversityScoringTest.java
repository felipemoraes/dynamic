package br.ufmg.dcc.latin.scoring.diversity;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

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
	 * Test case for xQuAD based on the example of Rodrygo Santos on his thesis.
	 */

	@Test
	public void xQuADScoreTest() {
		List<FlatAspect[]> coverage = new ArrayList<FlatAspect[]>();
		List<FlatAspect> importance = new ArrayList<FlatAspect>();
		List<FlatAspect> novelty = new ArrayList<FlatAspect>();
		importance.add(new FlatAspect(0.6f));
		importance.add(new FlatAspect(0.4f));
		novelty.add(new FlatAspect(1.0f));
		novelty.add(new FlatAspect(1.0f));
		coverage.add(new FlatAspect[5]);
		coverage.add(new FlatAspect[5]);
		coverage.get(0)[0] = new FlatAspect(0.3f);
		coverage.get(0)[1] = new FlatAspect(0.7f);
		coverage.get(0)[2] = new FlatAspect(0.2f);
		coverage.get(0)[3] = new FlatAspect(0.7f);
		coverage.get(0)[4] = new FlatAspect(0.4f);
		
		coverage.get(1)[0] = new FlatAspect(0.4f);
		coverage.get(1)[1] = new FlatAspect(0.6f);
		coverage.get(1)[2] = new FlatAspect(0.3f);
		coverage.get(1)[3] = new FlatAspect(0.8f);
		coverage.get(1)[4] = new FlatAspect(0.2f);
		
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
