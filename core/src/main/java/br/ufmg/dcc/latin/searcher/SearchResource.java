package br.ufmg.dcc.latin.searcher;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.MultiMatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;



public class SearchResource {
	
	
	private Client client;
	
	
	
	private String indexName;

	private String docType;
	
	public SearchResource(String indexName, String docType){
		
		
		this.indexName = indexName;
		this.docType = docType;
		
    	Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch")
    			.put("client.transport.ping_timeout", "30s")
    			.build();
		
		try {
			client = TransportClient.builder().settings(settings).build().
			        addTransportAddress(new InetSocketTransportAddress(
			           InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void search(String indexName, String query) {
		
    	try {
	    	Settings settings = Settings.settingsBuilder()
	    			.put("client.transport.ping_timeout", "30s")
	    			.put("cluster.name", "latin_elasticsearch").build();
	        Client client;
			
				client = TransportClient.builder().settings(settings).build().
				        addTransportAddress(new InetSocketTransportAddress(
				           InetAddress.getByName("localhost"), 9300));
			
	        SearchResponse response = client.prepareSearch(indexName)
	                .setTypes("doc")
	                //.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
	                .setQuery(QueryBuilders.queryStringQuery(query).field("text"))                 
	                .setFrom(0).setSize(1000)
	                .execute()
	                .actionGet();
	        
	        for (SearchHit hit : response.getHits()) {
	        //	searchPool.add(new DocScorePair(hit.getId(), hit.getScore()));
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
	
	public String buildDocLenScript(String field){
		String script =  "size=0; for (String term : _doc['" 
				+ field +"']) {size += _index['"+ field + "'][term].tf()};return size;";
		return script;
	}
	
	
	public String buildSumTtfScript(String field){
		String script =  "_index['"+ field + "'].sumttf()";
		return script;
	}
	
	public String buildDocCountScript(String field){
		String script =  "_index['"+ field + "'].docCount()";
		return script;
	}
	
	public String buildDfScript(String field,String terms){
		String script =  "HashMap<String,Long> dfs = new HashMap<String,Long>();"
				+ "for (term in " + terms + ") {"
				+ "dfs.put(term,_index['"+ field + "'][term].df());"
				+ "};return dfs;";
		return script;
	}

	public String buildTfScript(String field, String terms){
		String script =  "HashMap<String,Long> tfs = new HashMap<String,Long>();"
				+ "for (term in " +  terms + ") {"
				+ "tfs.put(term,_index['"+ field + "'][term].tf());"
				+ "};return tfs;";
		return script;
	}

	public String buildTtfScript(String field, String terms){
		String script =  "HashMap<String,Long> ttfs = new HashMap<String,Long>();"
				+ "for (term in "+terms+") {"
				+ "ttfs.put(term,_index['"+ field + "'][term].ttf());"
				+ "};return ttfs;";
		return script;
	}
	
	private String getQueryTerms(String query){
        AnalyzeRequest request = new AnalyzeRequest().text(query).analyzer("standard");
        List<AnalyzeResponse.AnalyzeToken> tokens = client.admin().indices().analyze(request).actionGet().getTokens();
        int n = tokens.size();
        String[] terms = new String[n];
        int i = 0;
        for (AnalyzeResponse.AnalyzeToken token : tokens){
        	terms[i] = token.getTerm();
        	i++;
        }
        
		String listTerms = "[";
		i = 0;
		for (; i < terms.length - 1; i++) {
			listTerms += "'" + terms[i] + "',"; 
		}
		listTerms += "'" + terms[i++]+ "']";
		return listTerms;
	}
	
	private int getDocLen(String doc){
		int n = 0;
		AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
		
		try {
			
			StandardTokenizer tokenizer = new StandardTokenizer(factory);
			tokenizer.setReader(new StringReader(doc));
			tokenizer.reset();
			// CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
			
		    while (tokenizer.incrementToken()) {
		        n++;
		    }
		    tokenizer.close();
		    
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n;
	}
	
	
	public ResultSet search(String query, String[] fields, float[] weights, int size) {
		//File f = new File(query + ".data");
		ResultSet resultSet = new ResultSet();
		/*if(f.exists() && !f.isDirectory()) { 
			resultSet.readFromFile(query);
			return resultSet;
		}*/
		
		MultiMatchQueryBuilder qb = QueryBuilders.multiMatchQuery(query, fields).type("most_fields");
		for(int i = 0; i < weights.length; ++i){
			qb.field(fields[i], weights[i]);
		}
		
		SearchRequestBuilder request = client.prepareSearch(indexName)
				.setTypes(docType)
				.setQuery(qb)
			//	.setQuery(QueryBuilders.queryStringQuery(query))
				.addFields(fields)
				.addField("docno")
				.addField("docid")
			
				.setFrom(0).setSize(size);
		
		
    	SearchResponse response = request.execute().actionGet();
    	if (response.getHits().getTotalHits() < size) {
    		size = (int) response.getHits().getTotalHits();
    	}
    	int i = 0;
    	int[] docIds = new int[size];
    	String[] docNos = new String[size];
    	String[] docContent = new String[size];
    	float[] scores = new float[size];
    	Posting[][] postings = new Posting[size][];
		for (SearchHit hit : response.getHits()) {
			
			docIds[i] =  Integer.parseInt(hit.getFields().get("docid").getValue());
			docNos[i] =  hit.getFields().get("docno").getValue();
			scores[i] = hit.getScore();
			docContent[i] = (String) hit.getFields().get(fields[0]).getValues().get(0);
			
			i++;	
		}	
		
		resultSet.setDocIds(docIds);
		resultSet.setScores(scores);
		resultSet.setPostings(postings);
		resultSet.setDocNos(docNos);
		resultSet.setDocContent(docContent);
		
		//resultSet.writeToFile(query);
		return resultSet;
	}
	
	public ResultSet search(String query, String[] fields, int size) {
		File f = new File(query + ".data");
		ResultSet resultSet = new ResultSet();
		if(f.exists() && !f.isDirectory()) { 
			resultSet.readFromFile(query);
			return resultSet;
		}
		
		//String terms = getQueryTerms(query);
		
		SearchRequestBuilder request = client.prepareSearch(indexName)
				.setTypes(docType)
				.setQuery(QueryBuilders.multiMatchQuery(query, fields).field("text", 0.7f).field("title",0.2f).field("anchor",0.1f))
			//	.setQuery(QueryBuilders.queryStringQuery(query))
				.addFields(fields)
				.addField("docno")
				.addField("docid")
			
				.setFrom(0).setSize(size);
		

		/*for (int i = 0; i < fields.length; i++) {
			request.addScriptField("sum_tff_" + fields[i], new Script(buildSumTtfScript(fields[i])));
			request.addScriptField("doc_count_" + fields[i], new Script(buildDocCountScript(fields[i])));
			request.addScriptField("tf_" + fields[i], new Script(buildTfScript(fields[i],terms)));
			request.addScriptField("df_" + fields[i], new Script(buildDfScript(fields[i],terms)));
			request.addScriptField("ttf_" + fields[i], new Script(buildTtfScript(fields[i],terms)));
		}*/

		
    	SearchResponse response = request.execute().actionGet();
    	if (response.getHits().getTotalHits() < size) {
    		size = (int) response.getHits().getTotalHits();
    	}
    	int i = 0;
    	int[] docIds = new int[size];
    	String[] docNos = new String[size];
    	String[] docContent = new String[size];
    	float[] scores = new float[size];
    	Posting[][] postings = new Posting[size][];
		for (SearchHit hit : response.getHits()) {
			
			docIds[i] =  Integer.parseInt(hit.getFields().get("docid").getValue());
			docNos[i] =  hit.getFields().get("docno").getValue();
			scores[i] = hit.getScore();
			docContent[i] = (String) hit.getFields().get(fields[0]).getValues().get(0);
			
			//postings[i] = new Posting[fields.length];
			/*
			for (int j = 0; j < fields.length; j++) {
				String doc = (String) hit.getFields().get(fields[j]).getValues().get(0);
				int docLen = getDocLen(doc);
				long sumTff = (long) hit.getFields().get("sum_tff_" + fields[j] ).getValues().get(0);
				long docCount =  (long) hit.getFields().get("doc_count_" + fields[j]).getValues().get(0);
				@SuppressWarnings("unchecked")
				Map<String,Integer> tf = (Map<String, Integer>) hit.getFields().get("tf_" + fields[j]).getValues().get(0);
				@SuppressWarnings("unchecked")
				Map<String,Long> df = (Map<String, Long>) hit.getFields().get("df_" + fields[j]).getValues().get(0);
				@SuppressWarnings("unchecked")
				Map<String,Long> ttf = (Map<String, Long>) hit.getFields().get("ttf_" + fields[j]).getValues().get(0);
				
				postings[i][j] = buildPosting(docLen, sumTff, docCount, tf, df, ttf);
							
			}*/
			i++;	
		}	
		
		resultSet.setDocIds(docIds);
		resultSet.setScores(scores);
		resultSet.setPostings(postings);
		resultSet.setDocNos(docNos);
		resultSet.setDocContent(docContent);
		
		resultSet.writeToFile(query);
		return resultSet;
	}

	/**
	 * 
	 */
	private Posting buildPosting(long docLen, long sumTff, long docCount, Map<String, Integer> tf, Map<String, Long> df,
			Map<String, Long> ttf) {
		Posting posting = new Posting();
		posting.setDocLen(docLen);
		posting.setSumTotalTermFrequency(sumTff);
		posting.setDocCount(docCount);
		String[] postingTerms = new String[tf.size()];
		int[] termFrequency = new int[tf.size()];
		long[] docFrequency = new long[df.size()];
		long[] totalTermFrequency = new long[ttf.size()];
		int j = 0;
		for (Entry<String,Integer> keyValue : tf.entrySet()) {
			postingTerms[j] = keyValue.getKey();
			termFrequency[j] = keyValue.getValue();
			j++;
		}
		j = 0;
		for (Entry<String,Long> keyValue : df.entrySet()) {
			postingTerms[j] = keyValue.getKey();
			docFrequency[j] = keyValue.getValue();
			j++;
		}
		j = 0;
		for (Entry<String,Long> keyValue : ttf.entrySet()) {
			postingTerms[j] = keyValue.getKey();
			totalTermFrequency[j] = keyValue.getValue();
			j++;
		}
		
		posting.setTerms(postingTerms);
		posting.setTermFrequency(termFrequency);
		posting.setDocFrequency(docFrequency);
		posting.setTotalTermFrequency(totalTermFrequency);
		return posting;
	}

	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}


}
