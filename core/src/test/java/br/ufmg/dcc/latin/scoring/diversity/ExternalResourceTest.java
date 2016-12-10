package br.ufmg.dcc.latin.scoring.diversity;

import static org.junit.Assert.*;

import org.apache.lucene.util.BytesRef;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;

public class ExternalResourceTest {

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
		ExternalKnowledgeCache.init();
		System.out.println(ExternalKnowledgeCache.msEntityLinkingCollection.getKeyWordScore("crude", 18223));
	}

}
