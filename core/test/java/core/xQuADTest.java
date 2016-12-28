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
import br.ufmg.dcc.latin.reranker.xQuAD;

public class xQuADTest {

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
		xQuAD xquad = new xQuAD(feedbackModeling);

		xquad.depth = 5;

		xquad.lambda = 0.5;
		xquad.coverage = new double[5][2];
		xquad.docids = new int[5];
		xquad.docnos = new String[5];
		xquad.relevance = new double[5];
		xquad.importance = new double[2];
		xquad.novelty = new double[2];
		xquad.importance[0] = 0.6;
		xquad.importance[1] = 0.4;
		
		xquad.novelty[0] = 1;
		xquad.novelty[1] = 1;
		
		xquad.relevance[0] = 0.7;
		xquad.relevance[1] = 0.5;
		xquad.relevance[2] = 0.3;
		xquad.relevance[3] = 0.2;
		xquad.relevance[4] = 0.1;
		
		for (int i = 0; i < 5; i++) {
			xquad.docids[i] = i+1;
			xquad.docnos[i] = Integer.toString(i+1);
		}
		xquad.coverage[0][0] = 0.3;
		xquad.coverage[0][1] = 0.4;
		
		xquad.coverage[1][0] = 0.7;
		xquad.coverage[1][1] = 0.6;
		
		xquad.coverage[2][0] = 0.2;
		xquad.coverage[2][1] = 0.3;
		
		xquad.coverage[3][0] = 0.7;
		xquad.coverage[3][1] = 0.8;
		
		xquad.coverage[4][0] = 0.4;
		xquad.coverage[4][1] = 0.2;
		xquad.selected = new BooleanSelectedSet(5);
		
		xquad.relevance = xquad.normalize(xquad.relevance);
		feedbackModeling.normalizeCoverage(xquad.coverage);
		
		ResultSet result = xquad.get();
		for (int i = 0; i < 5; i++) {
			System.out.println(result.docnos[i] + " " + result.scores[i]);
		}
	}

}
