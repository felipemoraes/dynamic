/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.json.JSONObject;



/**
 * @author Felipe Moraes
 *
 */
public class CollectionCandidates {
	
	//index, < field, docId , <term, frequency>>
	Map<String, CollectionTerms> collections;
	//index, field, stats
	Map<String, FieldStats> fieldStats;
	//index, field, term, stats
	Map<String, TermStats> termsStats;
	
	public CollectionCandidates(){
		collections = new HashMap<String, CollectionTerms>();
		fieldStats = new HashMap<String,FieldStats>();
		termsStats = new HashMap<String, TermStats>();
	}
	
	private void checkIndexExistsAndThenCreate(String index){
		if (!collections.containsKey(index)) {
			collections.put(index, new CollectionTerms());
		}
		if (!fieldStats.containsKey(index)) {
			fieldStats.put(index, new FieldStats());
		}
		if (!termsStats.containsKey(index)) {
			termsStats.put(index, new TermStats());
		}
	}
	
	public long getDocCount(String index, String field){
		return fieldStats.get(index).getDocCount(field);
	}
	
	public long getSumTotalTermFreq(String index, String field) {
		return fieldStats.get(index).getSumTotalTermFreq(field);
	}
	
	public long getSumDocFreq(String index, String field) {
		return fieldStats.get(index).getSumDocFreq(field);
	}
	
	public long getDocFreq(String index, String field, String term){
		return termsStats.get(index).getDocFreq(field, term);
	}
	
	public long getTotalTermFreq(String index, String field, String term){
		return termsStats.get(index).getTotalTermFreq(field, term);
	}
	
	public long getTermFreq(String index, String field, String docId, String term){
		return collections.get(index).getDocTerms(field, docId).getOrDefault(term, (long) 0);
	}
	
	public Map<String,Long> getTerms(String index, String field, String docId){
		return collections.get(index).getDocTerms(field, docId);
	}
	
	public long getDocLen(String index, String field, String docId){
		long docLen = 0;
		for (Entry<String,Long> termFreq : collections.get(index).getDocTerms(field, docId).entrySet()) {
			docLen += termFreq.getValue();
		}
		return docLen;
	}
	
	public void putDocCandidate(String index, String field, String type, String docId) throws IOException{
		checkIndexExistsAndThenCreate(index);
		if (checkDocExists(index,field,type,docId)) {
			return;
		}
		
    	Settings settings = Settings.settingsBuilder()
			.put("cluster.name", "latin_elasticsearch").build();
    	Client client;
	
		client = TransportClient.builder().settings(settings).build().
		        addTransportAddress(new InetSocketTransportAddress(
		           InetAddress.getByName("localhost"), 9300));
		
		
		TermVectorsResponse resp = client.prepareTermVectors(index, type, docId)
    		.setTermStatistics(true).setFieldStatistics(false)
    		.setPayloads(false).setOffsets(false).setPositions(false).setDfs(true)
    		.setSelectedFields(field).execute().actionGet();
	
		XContentBuilder builder = XContentFactory.jsonBuilder();
		builder.startObject();
		resp.toXContent(builder, ToXContent.EMPTY_PARAMS);
		builder.endObject();
	
		String json = XContentHelper.convertToJson(builder.bytes(), true);
		JSONObject obj = new JSONObject(json);
		
		System.out.println(json);
		JSONObject terms = obj.getJSONObject("term_vectors").getJSONObject(field).getJSONObject("terms");
		Map<String,Long> termFreqs = new HashMap<String,Long>();
		for (String term : JSONObject.getNames(terms)) {
			
			termsStats.get(index).setDocFreq(field, term, terms.getJSONObject(term).getLong("doc_freq"));
			termsStats.get(index).setTotalTermFreq(field, term, terms.getJSONObject(term).getLong("ttf"));
			
			// collections
			termFreqs.put(term, terms.getJSONObject(term).getLong("term_freq"));
			
		}
		
		collections.get(index).setDocTerms(field, docId, termFreqs);
		/*
		JSONObject fStats = obj.getJSONObject("term_vectors").getJSONObject(field).getJSONObject("field_statistics");
		
		fieldStats.get(index).setDocCount(field, fStats.getLong("doc_count"));
		fieldStats.get(index).setSumDocFreq(field, fStats.getLong("sum_doc_freq"));
		fieldStats.get(index).setSumTotalTermFreq(field, fStats.getLong("sum_ttf"));*/
		client.close();
	}

	/**
	 * 
	 */
	private boolean checkDocExists(String index, String field, String type, String docId) {
		if (collections.get(index).checkDocExist(field, docId)) {
			return true;
		}
		return false;
	}
	
}
