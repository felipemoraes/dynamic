package br.ufmg.dcc.latin.scoring.diversity;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.cache.AspectCache;
import br.ufmg.dcc.latin.cache.RerankerCache;
import br.ufmg.dcc.latin.diversity.FlatAspectManager;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;

public class AspectManagerTest {
	
	private static String[] docContent = {
			"C C C C C C",
			"C C C C B B",
			"C C B B B B",
			"D E F G G G",
			"C E F G G G",
			"B B B B D D",
			"B B B B F F",
			"B B C F F F",
			"B B B B E E",
			"C C B H H I"
	};

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
	public void AspectBuildingTest() {
		
		FlatAspectManager aspectManager = new FlatAspectManager(docContent);
		Passage[] passages = new Passage[2];
		passages[0] = new Passage();
		passages[1] = new Passage();
		passages[0].setAspectId("A1");
		passages[1].setAspectId("A2");
		passages[0].setRelevance(4);
		passages[1].setRelevance(2);
		passages[0].setText("C");
		passages[1].setText("B");
		Feedback[] feedbacks = new Feedback[2];
		feedbacks[0] = new Feedback();
		feedbacks[0].setPassages(passages);
		feedbacks[0].setOnTopic(true);
		feedbacks[1] = new Feedback();
		feedbacks[1].setOnTopic(false);
		RerankerCache.feedbacks = new Feedback[10];
		RerankerCache.feedbacks[0] = feedbacks[0];
		RerankerCache.feedbacks[1] = feedbacks[1];
		aspectManager.mining(feedbacks);
		
		assertEquals(0.00, AspectCache.coverage[1][0].getValue(),0.0);
		assertEquals(0.00, AspectCache.coverage[1][1].getValue(),0.0);
		assertEquals(0.6666667, AspectCache.coverage[0][0].getValue(),0.0001);
		assertEquals(0.33333334, AspectCache.coverage[0][1].getValue(),0.0001);
	}
	
	@Test
	public void AspectDivesityTest() {
		xQuAD divX = new xQuAD(AspectCache.importance,AspectCache.coverage,AspectCache.novelty);
		assertNotEquals(0, divX.div(0), 0);
	}

}
