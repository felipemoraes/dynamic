package br.ufmg.dcc.latin.scoring.diversity;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufmg.dcc.latin.aspect.external.QuerylogAspectMining;
import br.ufmg.dcc.latin.aspect.external.WikipediaAspectMining;
import br.ufmg.dcc.latin.external.EntityLinkingCollection;
import junit.framework.Assert;

public class ExternalAspectMiningTest {

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
		WikipediaAspectMining wikipediaAspectMining = new WikipediaAspectMining(20);
		QuerylogAspectMining querylogAspectMining = new QuerylogAspectMining(20);
		List<String> terms = new ArrayList<String>();
		terms.add("ebola");
	
		System.out.println(wikipediaAspectMining.computeTermWeights(terms, ""));
		System.out.println(querylogAspectMining.computeTermWeights(terms, ""));
		EntityLinkingCollection collection = new EntityLinkingCollection("../share/MicrosoftEntityLinkingData.txt");
		
	}

}
