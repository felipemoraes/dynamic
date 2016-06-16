package br.ufmg.dcc.latin.searcher;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import br.ufmg.dcc.latin.searcher.utils.Details;
import br.ufmg.dcc.latin.searcher.utils.Details;
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
	
	public Map<String,Double> initialSearch(String indexName, String query, Integer max) {
		Map<String,Double> resultSet = new HashMap<String,Double>();
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
	                .setNoFields()
	                .execute()
	                .actionGet();
	        for (SearchHit hit : response.getHits()) {
	        	resultSet.put(hit.getId(), (double) hit.getScore());
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

	public Map<String,Double> searchAndFilter(String indexName, String query, Set<String> ids) {
		
		Map<String,Double> resultSet = new HashMap<String,Double>();
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
	        
	        
			while (resultSet.size() < ids.size()) {
				
		        for (SearchHit hit : response.getHits()) {
		        	if (ids.contains(hit.getId())) {
		        		resultSet.put(hit.getId(), (double) hit.getScore());
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

	public HashMap<String,Details> searchDetails(String indexName, String query, Set<String> ids, WeightingModel model) {
		
		HashMap<String,Details> details = new HashMap<String,Details>();
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
	        
	        Integer counter = 0;
			while (counter < ids.size()) {
				
		        for (SearchHit hit : response.getHits()) {
		 
		        	if (ids.contains(hit.getId())) {
		        		counter++;
		        	} 
		        	
		        	Details modelDetails = model.getDetails(hit.getExplanation());
		        	if (modelDetails != null) {
		        		details.put(hit.getId(), modelDetails);
		        		
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
    	
		return details;
	}
	



	public Integer getCounter() {
		return counter;
	}


	public void setCounter(Integer counter) {
		this.counter = counter;
	}




}
