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
import br.ufmg.dcc.latin.reranker.PM2;
import br.ufmg.dcc.latin.reranker.xQuAD;

public class PM2Test {

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
		PM2 pm2 = new PM2(feedbackModeling);

		pm2.depth = 5;

		pm2.lambda = 0.5;
		pm2.coverage = new double[5][2];
		pm2.docids = new int[5];
		pm2.docnos = new String[5];
		pm2.relevance = new double[5];
		pm2.highestAspect = new int[5];
		pm2.s = new double[2];
		pm2.v = new double[2];
		pm2.v[0] = 0.6;
		pm2.v[1] = 0.4;
		
		pm2.s[0] = 0;
		pm2.s[1] = 0;
		
		pm2.relevance[0] = 0.7;
		pm2.relevance[1] = 0.5;
		pm2.relevance[2] = 0.3;
		pm2.relevance[3] = 0.2;
		pm2.relevance[4] = 0.1;
		
		for (int i = 0; i < 5; i++) {
			pm2.docids[i] = i+1;
			pm2.docnos[i] = Integer.toString(i+1);
			pm2.highestAspect[i] = -1;
		}
		pm2.coverage[0][0] = 0.3;
		pm2.coverage[0][1] = 0.4;
		
		pm2.coverage[1][0] = 0.7;
		pm2.coverage[1][1] = 0.6;
		
		pm2.coverage[2][0] = 0.2;
		pm2.coverage[2][1] = 0.3;
		
		pm2.coverage[3][0] = 0.7;
		pm2.coverage[3][1] = 0.8;
		
		pm2.coverage[4][0] = 0.4;
		pm2.coverage[4][1] = 0.2;
		pm2.selected = new BooleanSelectedSet(5);
		
		pm2.relevance = pm2.normalize(pm2.relevance);
		feedbackModeling.normalizeCoverage(pm2.coverage);
		
		ResultSet result = pm2.get();
		for (int i = 0; i < 5; i++) {
			System.out.println(result.docnos[i] + " " + result.scores[i]);
		}
	}

}
