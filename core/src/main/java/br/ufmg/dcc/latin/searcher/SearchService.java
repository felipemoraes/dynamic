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

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
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
	private String field;
	private String docType;
	
	public SearchService(String indexName, String field, String docType){
		this.counter = 0;
		searchPool = new ArrayList<DocScorePair>();
		
		this.indexName = indexName;
		this.field = field;
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
		this.setCounter(0);
    	try {
	    	Settings settings = Settings.settingsBuilder()
	    			.put("script.inline",true)
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
		try {
			Analyzer analyzer = new StandardAnalyzer();
			TokenStream stream  = analyzer.tokenStream(null, new StringReader(doc));
			stream.reset();
		    while (stream.incrementToken()) {
		        n++;
		    }
		    analyzer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return n;
	}
	
	public ResultSet search(String query, int size) {
		ResultSet resultSet = new ResultSet();
		String terms = getQueryTerms(query);
		
		SearchRequestBuilder request = client.prepareSearch(indexName)
				.setTypes("doc")
				.setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
				.setQuery(QueryBuilders.queryStringQuery(query).field("text"))
				.addFields("text")
				.setFrom(0).setSize(size);
		


		request.addScriptField("sum_tff", new Script(buildSumTtfScript("text")));
		request.addScriptField("doc_count", new Script(buildDocCountScript("text")));
		request.addScriptField("tf", new Script(buildTfScript("text",terms)));
		request.addScriptField("df", new Script(buildDfScript("text",terms)));
		request.addScriptField("ttf", new Script(buildTtfScript("text",terms)));
		
		
    	SearchResponse response = request.execute().actionGet();
    	int i = 0;
    	int[] docIds = new int[size];
    	float[] scores = new float[size];
    	Posting[] postings = new Posting[size];
		for (SearchHit hit : response.getHits()) {
			
			String doc = (String) hit.getFields().get("text").getValues().get(0);
			int docLen = getDocLen(doc);
			long sumTff = (long) hit.getFields().get("sum_tff").getValues().get(0);
			long docCount =  (long) hit.getFields().get("doc_count").getValues().get(0);
			
			@SuppressWarnings("unchecked")
			Map<String,Integer> tf = (Map<String, Integer>) hit.getFields().get("tf").getValues().get(0);
			@SuppressWarnings("unchecked")
			Map<String,Long> df = (Map<String, Long>) hit.getFields().get("df").getValues().get(0);
			@SuppressWarnings("unchecked")
			Map<String,Long> ttf = (Map<String, Long>) hit.getFields().get("ttf").getValues().get(0);
			
			docIds[i] =  Integer.parseInt(hit.getId());
			scores[i] = hit.getScore();
			postings[i] = buildPosting(docLen, sumTff, docCount, tf, df, ttf);
			i++;
		}	
		
		resultSet.setDocIds(docIds);
		resultSet.setScores(scores);
		resultSet.setPostings(postings);
		
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


	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.Searcher#search(java.lang.String, java.util.List)
	 */



	public Integer getCounter() {
		return counter;
	}


	public void setCounter(Integer counter) {
		this.counter = counter;
	}
	public String getField() {
		return field;
	}
	public void setField(String field) {
		this.field = field;
	}
	public String getDocType() {
		return docType;
	}
	public void setDocType(String docType) {
		this.docType = docType;
	}




}
