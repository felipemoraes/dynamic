package br.ufmg.dcc.latin.searcher;

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
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;



public class SearchService {
	
	
	private int counter;
	
	private Client client;
	
	private List<DocScorePair> searchPool;
	
	private String indexName;

	private String docType;
	
	public SearchService(String indexName, String field, String docType){
		this.counter = 0;
		searchPool = new ArrayList<DocScorePair>();
		
		this.indexName = indexName;
		this.docType = docType;
		
    	Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch").build();
		
		try {
			client = TransportClient.builder().settings(settings).build().
			        addTransportAddress(new InetSocketTransportAddress(
			           InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//CHANGEME
	public SearchService(){
		this.counter = 0;
		searchPool = new ArrayList<DocScorePair>();
		
    	Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch").build();
		
		try {
			client = TransportClient.builder().settings(settings).build().
			        addTransportAddress(new InetSocketTransportAddress(
			           InetAddress.getByName("localhost"), 9300));
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	

	
	
	public Map<String, Float> getNextResults(){
		if (searchPool.size() == counter){
			return null;
		}
		Map<String, Float>  result = new HashMap<String, Float> ();
		for (int i = counter; i < counter + 5; i++) {
			result.put(searchPool.get(i).getDocId(), searchPool.get(i).getScore());
		}
		counter += 5;
		return result;
	}
	
	public void search(String indexName, String query) {
		this.counter = 0;
    	try {
	    	Settings settings = Settings.settingsBuilder()
	    			.put("client.transport.ping_timeout", "30s")
	    			.put("client.transport.nodes_sampler_interval","30s")
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
	        searchPool.clear();
	        for (SearchHit hit : response.getHits()) {
	        	searchPool.add(new DocScorePair(hit.getId(), hit.getScore()));
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
	
	public ResultSet search(String query, String[] fields, int size) {
		ResultSet resultSet = new ResultSet();
		String terms = getQueryTerms(query);
		
		SearchRequestBuilder request = client.prepareSearch(indexName)
				.setTypes(docType)
				.setQuery(QueryBuilders.queryStringQuery(query).field(fields[0]))
				.addFields(fields)
				.addField("docno")
				.addField("docid")
				.setFrom(0).setSize(size);
		

		for (int i = 0; i < fields.length; i++) {
			request.addScriptField("sum_tff_" + fields[i], new Script(buildSumTtfScript(fields[i])));
			request.addScriptField("doc_count_" + fields[i], new Script(buildDocCountScript(fields[i])));
			request.addScriptField("tf_" + fields[i], new Script(buildTfScript(fields[i],terms)));
			request.addScriptField("df_" + fields[i], new Script(buildDfScript(fields[i],terms)));
			request.addScriptField("ttf_" + fields[i], new Script(buildTtfScript(fields[i],terms)));
		}

		
    	SearchResponse response = request.execute().actionGet();
    	int i = 0;
    	int[] docIds = new int[size];
    	String[] docNos = new String[size];
    	float[] scores = new float[size];
    	Posting[][] postings = new Posting[size][];
		for (SearchHit hit : response.getHits()) {
			
			docIds[i] =  Integer.parseInt(hit.getId());
			docNos[i] =  hit.getFields().get("docno").getValue();
			scores[i] = hit.getScore();
			postings[i] = new Posting[fields.length];

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
							
			}
			i++;	
		}	
		
		resultSet.setDocIds(docIds);
		resultSet.setScores(scores);
		resultSet.setPostings(postings);
		resultSet.setDocNos(docNos);
		
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


}
