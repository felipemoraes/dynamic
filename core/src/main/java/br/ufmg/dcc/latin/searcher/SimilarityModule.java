package br.ufmg.dcc.latin.searcher;

import java.net.InetAddress;
import java.net.UnknownHostException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;


import br.ufmg.dcc.latin.searcher.similarity.Similarity;
import br.ufmg.dcc.latin.searcher.similarity.*;
public class SimilarityModule {
	
	public SimilarityModule(String indexName) throws UnknownHostException{
		DefaultSimilarity defaultSimilarity = null;
		changeSimilarityModule(indexName, defaultSimilarity);
	}
	
	public SimilarityModule(){
	}
	
	public void changeSimilarityModule(String indexName, Similarity similarity) throws UnknownHostException{
		Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch").build();
		
		
        Settings simSettings = similarity.getSettings();
	
        Client client = TransportClient.builder().settings(settings).build().
                addTransportAddress(new InetSocketTransportAddress(
                   InetAddress.getByName("localhost"), 9300));
        
        client.admin().indices().prepareClose(indexName).execute().actionGet();
        client.admin().indices().prepareUpdateSettings(indexName).setSettings(simSettings).execute().actionGet();
        client.admin().indices().prepareOpen(indexName).execute().actionGet();
        indexAvailable(indexName, client);
		client.close();
	}
	

	private void indexAvailable(String indexName, Client client) {
		while (true) {
        	try {
                client.prepareSearch(indexName)          
                	.setQuery(QueryBuilders.matchAllQuery())                 
                    .setFrom(0).setSize(1)
                    .execute()
                    .actionGet();	
				break;
			} catch (Exception e) {
			}
		
		}
	}

}
