package br.ufmg.dcc.latin.models;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


import br.ufmg.dcc.latin.search.elements.Feedback;
import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.SearchResource;

public class Baseline {

	private int[] docIds;
	private String[] docNos;
	private double[] relevance;
	
	private HashMap<Integer,Integer> mapIds;
	private Set<Integer> selected;
	
	private SearchResource searchResource;
	
	
	public void create(String index, String query) {
		if (searchResource == null) {
			searchResource = new SearchResource(index,"doc");
		} else if (!searchResource.getIndexName().equals(index)){
			searchResource.setIndexName(index);
		}
		
		String[] fields = {"text","title","anchor"};
		float[] weights = {0.6f,0.3f,0.1f};
				
		ResultSet resultSet = searchResource.search(query,fields,weights, 5000);
		System.out.println("Retrieved");
		docIds = resultSet.getDocIds();
		docNos = resultSet.getDocNos();
		
		
		relevance = new double[docIds.length];
		for (int i = 0; i < relevance.length; ++i){
			relevance[i] = (double) resultSet.getScores()[i];
		}
		
		mapIds = new HashMap<Integer,Integer>();
		selected = new HashSet<Integer>();
		
		for (int i = 0; i < docIds.length; ++i){
			mapIds.put(docIds[i], i);
		}

	}

	
	public Map<String, Double> get() {

		Map<String, Double> result = new HashMap<String, Double>();
		
		while (result.size() < 5 && selected.size() < docIds.length){
			int maxRank = -1;
			double maxScore = Double.NEGATIVE_INFINITY;
			for (int i = 0; i < relevance.length; i++) {
				if (selected.contains(i)){
					continue;
				}
				if (maxScore < relevance[i]) {
					maxRank = i;
					maxScore = relevance[i];
				}
			}
			selected.add(maxRank);
			result.put(docNos[maxRank], maxScore);
			
		}
		
		return result;
	}



}
