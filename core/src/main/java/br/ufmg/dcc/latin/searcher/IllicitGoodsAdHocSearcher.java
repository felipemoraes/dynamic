package br.ufmg.dcc.latin.searcher;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import br.ufmg.dcc.latin.searcher.models.WeightingModel;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;

public class IllicitGoodsAdHocSearcher extends AdHocSearcher implements Searcher {

	public IllicitGoodsAdHocSearcher(String indexName,WeightingModel similarity) throws UnknownHostException {
		super(indexName,similarity);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void search(String query) {
		super.setCounter(0);
    	try {
    	
		 
	    	Settings settings = Settings.settingsBuilder()
	    			.put("cluster.name", "latin_elasticsearch").build();
	        Client client;
			
				client = TransportClient.builder().settings(settings).build().
				        addTransportAddress(new InetSocketTransportAddress(
				           InetAddress.getByName("localhost"), 9300));
			
	        SearchResponse response = client.prepareSearch(indexName)
	                .setTypes("trec-dd")
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.queryStringQuery(query))                 
	                .setFrom(0).setSize(10)
	                .execute()
	                .actionGet();
	        searchPool.clear();
	        for (SearchHit hit : response.getHits()) {
	        	searchPool.add(new DocScorePair(hit.getId(), (double) hit.getScore()));
			}
	        client.close();
	        
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.Searcher#search(java.lang.String, java.lang.Integer)
	 */
	@Override
	public ResultSet search(String query, Integer max) {
		ResultSet resultSet = new ResultSet();
    	try {
	    	Settings settings = Settings.settingsBuilder()
	    			.put("cluster.name", "latin_elasticsearch").build();
	        Client client;
			
				client = TransportClient.builder().settings(settings).build().
				        addTransportAddress(new InetSocketTransportAddress(
				           InetAddress.getByName("localhost"), 9300));
			
	        SearchResponse response = client.prepareSearch(indexName)
	                .setTypes("doc")
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.queryStringQuery(query))                 
	                .setFrom(0).setSize(max)
	                .execute()
	                .actionGet();
	        for (SearchHit hit : response.getHits()) {
	        	resultSet.getResultSet().put(hit.getId(), (double) hit.getScore());
			}	
	        client.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return resultSet;
	}

	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.Searcher#search(java.lang.String, java.util.List)
	 */
	@Override
	public ResultSet search(String query, Set<String> ids) {
		ResultSet resultSet = new ResultSet();
    	try {
	    	Settings settings = Settings.settingsBuilder()
	    			.put("cluster.name", "latin_elasticsearch").build();
	        Client client;
			
				client = TransportClient.builder().settings(settings).build().
				        addTransportAddress(new InetSocketTransportAddress(
				           InetAddress.getByName("localhost"), 9300));
			Integer start = 0;
			Integer end = 1000;
			while (resultSet.getResultSet().size() < ids.size()){
		        SearchResponse response = client.prepareSearch(indexName)
		                .setTypes("doc")
		                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		                .setQuery(QueryBuilders.queryStringQuery(query).field("body"))                 
		                .setFrom(start).setSize(end)
		                .execute()
		                .actionGet();
		        if (response.getHits().getTotalHits() == 0) {
					break;
				}
		        
		        for (SearchHit hit : response.getHits()) {
		        	if (ids.contains(hit.getId())) {
		        		resultSet.getResultSet().put(hit.getId(), (double) hit.getScore());
		        	}
				}	
			}

	        client.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return resultSet;
	}


	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.Searcher#search(java.lang.String, java.util.Set, br.ufmg.dcc.latin.searcher.models.WeightingModel)
	 */
	@Override
	public ResultSet search(String query, Set<String> ids, WeightingModel weightingModel) {
		// TODO Auto-generated method stub
		return null;
	}


}
