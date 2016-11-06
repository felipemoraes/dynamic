package br.ufmg.dcc.latin.system;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.system.TrecUser;
import br.ufmg.dcc.latin.system.xQuADSession;

public class xQuADSessionTest {

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
		xQuADSession session = new xQuADSession();
		session.start("ebola16", "DD16-1", "US Military Crisis Response");
		session.run();
	}

}
