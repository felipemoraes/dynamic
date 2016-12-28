package core;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.BooleanSelectedSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranker.xMMR;

public class xMMRTest {

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
		FeedbackModeling feedbackModeling = new FeedbackModeling();
		xMMR xmmr = new xMMR(feedbackModeling);
		xmmr.cacheSim = new double[5];
		xmmr.depth = 5;
		xmmr.n = 5;
		xmmr.lambda = 0.5;
		xmmr.coverage = new double[5][2];
		xmmr.docids = new int[5];
		xmmr.docnos = new String[5];
		xmmr.relevance = new double[5];
		
		xmmr.relevance[0] = 0.1;
		xmmr.relevance[1] = 0.1;
		xmmr.relevance[2] = 0.1;
		xmmr.relevance[3] = 0.1;
		xmmr.relevance[4] = 0.1;
		
		for (int i = 0; i < 5; i++) {
			xmmr.docids[i] = i+1;
			xmmr.docnos[i] = Integer.toString(i+1);
		}
		
		xmmr.coverage[0][0] = 0.5;
		xmmr.coverage[0][1] = 0.5;
		
		xmmr.coverage[1][0] = 0.5;
		xmmr.coverage[1][1] = 0.5;
		
		xmmr.coverage[2][0] = 0.3;
		xmmr.coverage[2][1] = 0.1;
		
		xmmr.coverage[3][0] = 0.2;
		xmmr.coverage[3][1] = 0.8;
		
		xmmr.coverage[4][0] = 0.4;
		xmmr.coverage[4][1] = 0.2;
		xmmr.selected = new BooleanSelectedSet(5);
		
		xmmr.relevance = xmmr.normalize(xmmr.relevance);
		
		
		ResultSet result = xmmr.get();
		for (int i = 0; i < 5; i++) {
			System.out.println(result.docnos[i] + " " + result.scores[i]);
		}
	}

}
