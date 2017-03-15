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

import br.ufmg.dcc.latin.cache.RetrievalCache;
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
		TrecUser.load("../share/truth_data.txt");
		List<double[]> params = new ArrayList<double[]>();
		double[] param = {1000d, 0.5d};

		params.add(param);
		Session session = new Session();
		session.setParams(params);
		RetrievalCache.topicId = "DD16-1";
		RetrievalCache.indexName = "ebola16";
		session.setReranker("PM2 PassageAspectMining");
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
