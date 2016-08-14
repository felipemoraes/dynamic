package br.ufmg.dcc.latin.controlers;


import br.ufmg.dcc.latin.search.elements.Feedback;

public interface SearchController {
	SearchResponse searchQuery(String index, String model, String query, Integer token);
	void updateFeedback(Feedback[] feedback, Integer token);
}
