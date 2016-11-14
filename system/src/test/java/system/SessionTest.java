package system;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.dynamicsystem.Session;
import br.ufmg.dcc.latin.dynamicsystem.TrecUser;

public class SessionTest {

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
		List<float[]> params = new ArrayList<float[]>();
		float[] param = {1000f, 0.8f};
		params.add(param);
		Session session = new Session();
		session.setParams(params);
		
		session.setScorer("xQuAD");
		session.run("US Military Crisis Response", "ebola16", "DD16-1");
		
		//session.setScorer("PM2");
		//session.run("US Military Crisis Response", "ebola16", "DD16-1");
		
		//session.setScorer("MMR");
		//session.run("US Military Crisis Response", "ebola16", "DD16-1");
		
		//session.setScorer("xMMR");
		//session.run("US Military Crisis Response", "ebola16", "DD16-1");
	}

}
