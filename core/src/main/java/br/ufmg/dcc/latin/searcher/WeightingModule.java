package br.ufmg.dcc.latin.searcher;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Set;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import br.ufmg.dcc.latin.searcher.models.*;
public class WeightingModule {
	

	public WeightingModule(){
	}
	
	public void changeWeightingModel(Set<String> indicesName, WeightingModel weightingModel) throws UnknownHostException{
		Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch").build();
		
		
        Settings simSettings = weightingModel.getSettings();
	
        Client client = TransportClient.builder().settings(settings).build().
                addTransportAddress(new InetSocketTransportAddress(
                   InetAddress.getByName("localhost"), 9300));
        
        for (String indexName : indicesName) {
        	client.admin().cluster().prepareHealth().setWaitForYellowStatus().get();
            client.admin().indices().prepareClose(indexName).execute().actionGet();
            client.admin().indices().prepareUpdateSettings(indexName).setSettings(simSettings).execute().actionGet();
            client.admin().indices().prepareOpen(indexName).execute().actionGet();
            client.admin().cluster().prepareHealth().setWaitForYellowStatus().get();
		}
        client.close();
	}
	
	public void changeWeightingModel(String indexName, WeightingModel weightingModel) throws UnknownHostException{
		Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch").build();
		
		
        Settings simSettings = weightingModel.getSettings();
	
        Client client = TransportClient.builder().settings(settings).build().
                addTransportAddress(new InetSocketTransportAddress(
                   InetAddress.getByName("localhost"), 9300));
        
        client.admin().indices().prepareClose(indexName).execute().actionGet();
        client.admin().indices().prepareUpdateSettings(indexName).setSettings(simSettings).execute().actionGet();
        client.admin().indices().prepareOpen(indexName).execute().actionGet();
        client.admin().cluster().prepareHealth().setWaitForYellowStatus().get();
		client.close();
		
	}
	



}
