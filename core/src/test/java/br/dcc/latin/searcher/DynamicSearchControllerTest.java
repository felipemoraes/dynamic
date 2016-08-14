package br.dcc.latin.searcher;

import static org.junit.Assert.*;



import org.junit.Test;

import br.ufmg.dcc.latin.controlers.DynamicSearchController;
import br.ufmg.dcc.latin.controlers.SearchResponse;


public class DynamicSearchControllerTest {

	@Test
	public void test() {

		DynamicSearchController controller = new DynamicSearchController();
		Integer token = null;
		SearchResponse response = controller.searchQuery("ebola_2015", "xQuADi", "ebola songs", token);
		System.out.println(response);
		response = controller.searchQuery("ebola_2015", "xQuADi", "ebola songs", 0);
		System.out.println(response);
	}

}
