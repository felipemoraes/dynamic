package br.ufmg.dcc.latin.controlers;


import java.util.Map;

public class SearchResponse {
	private Integer token;
	private Map<String,Double> response;
	public Integer getToken() {
		return token;
	}
	public void setToken(Integer token) {
		this.token = token;
	}
	public Map<String,Double> getResponse() {
		return response;
	}
	public void setResponse(Map<String,Double> response) {
		this.response = response;
	}
}
