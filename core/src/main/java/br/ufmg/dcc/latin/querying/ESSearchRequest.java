package br.ufmg.dcc.latin.querying;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

public class ESSearchRequest implements SearchRequest {
	
	private static ESSearchRequest instance;
	
	private Client client;
	
	private ESSearchRequest(){
		
    	Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch")
    			.put("client.transport.ping_timeout", "30s")
    			.build();
		
		try {
			client = TransportClient.builder().settings(settings).build().
			        addTransportAddress(new InetSocketTransportAddress(
			           InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		
		
	}
	
	public static ESSearchRequest getInstance(){
		if (instance == null) {
			instance = new ESSearchRequest();
		}
		return instance;
	}
	
	

	@Override
	public ResultSet search(QueryRequest query) {

		int size = 1000;
		
		MultiMatchQueryBuilder qb = QueryBuilders.multiMatchQuery(query.getQuery(), query.getFields()).type("most_fields");
		for(int i = 0; i < query.getFields().length; ++i){
			qb.field(query.getFields()[i], query.getFieldWeights()[i]);
		}
		
		SearchRequestBuilder request = client.prepareSearch(query.getIndex())
				.setTypes(query.getDocType())
				.setQuery(qb)
				.addFields(query.getFields())
				.addField("docno")
				.addField("docid")
			
				.setFrom(0).setSize(size);
		
		
    	SearchResponse response = request.execute().actionGet();
    	if (response.getHits().getTotalHits() < size) {
    		size = (int) response.getHits().getTotalHits();
    	}
    	int i = 0;
    	int[] docids = new int[size];
    	String[] docnos = new String[size];
    	String[] docsContent = new String[size];
    	float[] scores = new float[size];
    	
		for (SearchHit hit : response.getHits()) {
			
			docids[i] =  Integer.parseInt(hit.getFields().get("docid").getValue());
			docnos[i] =  hit.getFields().get("docno").getValue();
			scores[i] = hit.getScore();
			docsContent[i] = (String) hit.getFields().get(query.getFields()[0]).getValues().get(0);
			i++;	
		}	
		
		CollectionResultSet resultSet = new CollectionResultSet();
		
		resultSet.setDocids(docids);
		resultSet.setScores(scores);
		resultSet.setDocsContent(docsContent);
	
		return resultSet;
	}

}
