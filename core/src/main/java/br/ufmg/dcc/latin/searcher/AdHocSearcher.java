package br.ufmg.dcc.latin.searcher;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.search.sort.SortParseElement;

import br.ufmg.dcc.latin.searcher.models.WeightingModel;
import br.ufmg.dcc.latin.searcher.utils.PropertyDetails;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;

public class AdHocSearcher {
	
	
	private Integer counter;
	private List<DocScorePair> searchPool;
	
	public AdHocSearcher(String indexName){
		this.counter = 0;
		searchPool = new ArrayList<DocScorePair>();

	}
	
	public AdHocSearcher() {
		this.counter = 0;
		searchPool = new ArrayList<DocScorePair>();
	}

	
	public ResultSet getNextResults(){
		if (searchPool.size() == counter){
			return null;
		}
		ResultSet result = new ResultSet();
		for (int i = counter; i < counter + 5; i++) {
			result.putResult(searchPool.get(i).getDocId(),searchPool.get(i).getScore());
		}
		counter += 5;
		return result;
	}
	
	public void search(String indexName, String query) {
		this.setCounter(0);
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
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))                 
	                .setFrom(0).setSize(1000)
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
	
	public ResultSet initialSearch(String indexName, String query, Integer max) {
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
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))                 
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

	public ResultSet searchAndFilter(String indexName, String query, Set<String> ids) {
		
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
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))
	                .setSize(0)
	                .setPostFilter(QueryBuilders.idsQuery().addIds(ids))
	                .execute()
	                .actionGet();
			Integer total = (int) response.getHits().getTotalHits();
			
	        response = client.prepareSearch(indexName)
	                .setTypes("doc")
	                .addSort(SortParseElement.SCORE_FIELD_NAME, SortOrder.DESC)
	                .setScroll(new TimeValue(60000))
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))
	                .setPostFilter(QueryBuilders.idsQuery().addIds(ids))
	                .setSize(total)
	                .execute()
	                .actionGet();
	        
	        
			while (resultSet.getResultSet().size() < ids.size()) {
				
		        for (SearchHit hit : response.getHits()) {
		        	if (ids.contains(hit.getId())) {
		        		resultSet.getResultSet().put(hit.getId(), (double) hit.getScore());
		        	} 
				}
		        response = client.prepareSearchScroll(response.getScrollId()).
		        	setScroll(new TimeValue(60000)).execute().actionGet();
		        if (response.getHits().getHits().length == 0) {
		        	client.close();
		            break;
		        }
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return resultSet;
	}

	


	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.Searcher#search(java.lang.String, java.util.Set, java.lang.Boolean)
	 */

	public ResultSet searchAndFilterWithDetails(String indexName, String query, Set<String> ids, WeightingModel model) {
		
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
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))
	                .setSize(0)
	                .setPostFilter(QueryBuilders.idsQuery().addIds(ids))
	                .execute()
	                .actionGet();
			Integer total = (int) response.getHits().getTotalHits();
			
	        response = client.prepareSearch(indexName)
	                .setTypes("doc")
	                .addSort(SortParseElement.SCORE_FIELD_NAME, SortOrder.DESC)
	                .setScroll(new TimeValue(60000))
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))
	                .setPostFilter(QueryBuilders.idsQuery().addIds(ids))
	                .setSize(total)
	                .setExplain(true)
	                .execute()
	                .actionGet();
	        
	        
			while (resultSet.getResultSet().size() < ids.size()) {
				
		        for (SearchHit hit : response.getHits()) {
		        	if (ids.contains(hit.getId())) {
		        		resultSet.getResultSet().put(hit.getId(), (double) hit.getScore());
		        	} 
		        	
		        	PropertyDetails propertyDetails = model.getDetails(hit.getExplanation());
		        	if (propertyDetails != null) {
		        		resultSet.getDetails().getDocsDetails().put(hit.getId(), propertyDetails);
		        	}
				}
		        response = client.prepareSearchScroll(response.getScrollId()).
		        	setScroll(new TimeValue(60000)).execute().actionGet();
		        if (response.getHits().getHits().length == 0) {
		        	client.close();
		            break;
		        }
			}
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return resultSet;
	}


	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.Searcher#search(java.lang.String, int, br.ufmg.dcc.latin.searcher.models.WeightingModel)
	 */
	

	
	
	public ResultSet searchWithDetails(String indexName, String query, Integer max, WeightingModel weightingModel) {
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
	               // .addSort(SortParseElement.SCORE_FIELD_NAME, SortOrder.DESC)
	                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))
	                .setFrom(0).setSize(max)
	                .setExplain(true)
	                .execute()
	                .actionGet();
		        			
		        
	        for (SearchHit hit : response.getHits()) {
	        	System.out.println(hit.getId());
        		PropertyDetails propertyDetails = weightingModel.getDetails(hit.getExplanation());
        		if (propertyDetails != null) {
        			resultSet.getDetails().getDocsDetails().put(hit.getId(), propertyDetails);
				}
        		
        		resultSet.getResultSet().put(hit.getId(), (double) hit.getScore());
	        	
			}
	 
			

	        client.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
		return resultSet;
	}



	public Integer getCounter() {
		return counter;
	}


	public void setCounter(Integer counter) {
		this.counter = counter;
	}




}
