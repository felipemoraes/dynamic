package br.ufmg.dcc.latin.system;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.system.session.LMSession;
import br.ufmg.dcc.latin.system.session.MMRSession;

public class LMSessionTest {

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
		TrecUser.load("../share/truth_data_deduped.txt");
		LMSession session = new LMSession();
		session.start("ebola16", "DD16-1", "US Military Crisis Response");
		float[] params = {1000.0f, 0.5f};
		session.run("testLM", params);
	}

}
