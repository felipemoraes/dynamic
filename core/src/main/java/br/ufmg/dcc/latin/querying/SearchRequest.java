package br.ufmg.dcc.latin.querying;

public interface SearchRequest {
	ResultSet search(QueryRequest queryTerms);
}
