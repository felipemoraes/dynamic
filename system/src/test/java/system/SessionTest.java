package system;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.search.similarities.ClassicSimilarity;
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
		float[] param = {100f, 0.5f};

		params.add(param);
		Session session = new Session();
		session.setParams(params);
		
		session.setReranker("xQuAD");
		session.run( "ebola16", "DD16-1", "US Military Crisis Response");
		

		//session.setReranker("xMMR");
		//session.run("US Military Crisis Response", "ebola16", "DD16-1");
		
		//session.setReranker("LM");
		//session.run("US Military Crisis Response", "ebola16", "DD16-1");
		
		//session.setReranker("DPH");
		//session.run("illicit_goods", "DD15-102", "Telemarketing");
		
		//session.setReranker("BM25");
		//session.run("US Military Crisis Response", "ebola16", "DD16-1");
	}

}
