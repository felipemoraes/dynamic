package br.ufmg.dcc.latin.models.fsdm;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.AttributeFactory;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.fieldstats.FieldStats;
import org.elasticsearch.action.fieldstats.FieldStatsAction;
import org.elasticsearch.action.fieldstats.FieldStatsRequestBuilder;
import org.elasticsearch.action.fieldstats.FieldStatsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.analysis.CustomAnalyzer;
import org.elasticsearch.index.analysis.CustomAnalyzerProvider;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;
import org.elasticsearch.search.SearchHits;
import org.json.JSONObject;

public class QueryProcessor {
	
	private Client client;
	
	private String indexName;
	public String getIndexName() {
		return indexName;
	}

	public void setIndexName(String indexName) {
		this.indexName = indexName;
	}

	private ArrayList<String> fields;
	
	final static Charset ENCODING = StandardCharsets.UTF_8;
	private LinkedHashMap<String, String> queries = new LinkedHashMap<String, String>();
	private SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
	
	public QueryProcessor(){
		
	}
	
	public QueryProcessor(Boolean connectToElastic, String indexName, ArrayList<String> fields){
		this.indexName = indexName;
		this.fields = fields;
		
		if(connectToElastic){
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
	}
	
	public ArrayList<String> getFields(){
		return fields;
	}
	
	public HashMap<String, String> getQueries(){
		return queries;
	}
	
	public void parseQueriesFile(String queriesFilePath){
		try{
			Path path = Paths.get(queriesFilePath);
			String line;
		    try(Scanner scanner =  new Scanner(path, ENCODING.name())){
	    		while (scanner.hasNextLine()){
					line = scanner.nextLine();

					String[] parts = line.split("\t",2);
					String queryId = parts[0];
					String query = parts[1].trim();
					query = query.replaceAll("[\\!\\?\\)\\(\\,]", "").replace("'s", "").replace("'", "");
					queries.put(queryId, query);
					//System.out.println(queryId + " - " + query);
						  
		    	}      
		    }
		}
		catch(Exception e){
			System.out.println("Erro ao processar arquivo de queries: "+e.getMessage());
		}
	}
	
	public ArrayList<String> tokenizeQuery(String query, String analyzerName){
		
		ArrayList<String> terms = new ArrayList<String>();
		
		 AnalyzeRequest request = new AnalyzeRequest().text(query).analyzer(analyzerName).index(indexName);
		 
		List<AnalyzeResponse.AnalyzeToken> tokens = client.admin().indices().analyze(request).actionGet().getTokens();
		for (AnalyzeResponse.AnalyzeToken token : tokens){
			terms.add(token.getTerm());
		}
		
		return terms;
	}
	
	public int getDocLen(String doc){
		int n = 0;
		AttributeFactory factory = AttributeFactory.DEFAULT_ATTRIBUTE_FACTORY;
		
		try {
			StandardTokenizer tokenizer = new StandardTokenizer(factory);
			tokenizer.setReader( new StringReader(doc));
			tokenizer.reset();
			// CharTermAttribute attr = tokenizer.addAttribute(CharTermAttribute.class);
			
		    while (tokenizer.incrementToken()) {
		        n++;
		    }
		    tokenizer.close();
		    
		} catch (IOException e) {
			e.printStackTrace();
		}
		return n;
	}
	
	public HashMap<String, JSONObject> getBiTermsFrenqueciesOnCollection(ArrayList<String> terms){
		return getTermsFrenqueciesOnCollection(terms, ".bigrams");
	}
	
	public HashMap<String, JSONObject> getTermsFrenqueciesOnCollection(ArrayList<String> terms){
		return getTermsFrenqueciesOnCollection(terms, "");
	}
	
	public HashMap<String, JSONObject> getTermsFrenqueciesOnCollection(ArrayList<String> terms, String sufix){
		HashMap<String, JSONObject> frequencies = new HashMap<String, JSONObject>();
		
		SearchRequestBuilder request = client.prepareSearch(indexName)
		        .setTypes("doc")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH);
		
		for(String field : fields){
			ArrayList<String> ttfs = new ArrayList<String>();
			for(String term : terms){
				ttfs.add("'"+term+"': _index['"+field+sufix+"']['"+term+"'].ttf()");
			}
			request.addScriptField(field, new Script(StringUtils.join(ttfs)));
			
		}
		
		SearchResponse response = request.setFrom(0).setSize(1)
							        	 .setExplain(true)
							        	 .execute()
							        	 .actionGet();
		
		for (Map.Entry<String, SearchHitField> entry : response.getHits().getHits()[0].getFields().entrySet()) {
			String fieldName = entry.getKey();
			SearchHitField fieldHit = entry.getValue();
			JSONObject json;
			
			
			if(fieldHit.getValue() == null || fieldHit.getValue().toString().equals("[]"))
				json = new JSONObject("{}");
			else
				json = new JSONObject(fieldHit.getValue().toString().replace("=", ":"));
			frequencies.put(fieldName, json);
		}
		
		return frequencies;
	}
	
	/*public HashMap<String, JSONObject> getWindowBiTermsFrenqueciesOnCollection(ArrayList<String> terms, HashMap<String, JSONObject> fieldsStats){
		for(String field : fields){
			int tfFieldColl = fieldsStats.get(field).getInt("doc_count") / 100 * 2; //same as Ivory system
		}
	}*/
	
	
	public SearchHits searchEntities(String query, ArrayList<String> uTerms, ArrayList<String> bTerms, int limit){
		String[] fieldsArr = new String[fields.size()];
		fieldsArr = fields.toArray(fieldsArr);
		
		String[] fieldsToSelect = new String[fields.size() + 1];
		fieldsToSelect = fields.toArray(fieldsToSelect);
		fieldsToSelect[fields.size()] = "docno";
		
		SearchRequestBuilder request = client.prepareSearch(indexName)
		        .setTypes("doc")
		        .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
		        .setQuery(
		        	QueryBuilders.boolQuery().should(
		        		QueryBuilders.multiMatchQuery(query, fieldsArr)
		        	)
		        ).addFields(fieldsToSelect);
		
		//unigrams
		for(String field : fields){
			ArrayList<String> tfs = new ArrayList<String>();
			for(String term : uTerms){
				tfs.add("'"+term+"': _index['"+field+"']['"+term+"'].tf()");
			}
			request.addScriptField("tf_"+field, new Script(StringUtils.join(tfs)));
		}
		
		//bigrams
		for(String field : fields){
			ArrayList<String> tfs = new ArrayList<String>();
			for(String term : bTerms){
				tfs.add("'"+term+"': _index['"+field+".bigrams']['"+term+"'].tf()");
			}
			request.addScriptField("tf_"+field+".bigrams", new Script(StringUtils.join(tfs)));
		}
		
		SearchResponse response = request.setFrom(0).setSize(limit)
							        	 .setExplain(true)
							        	 .execute()
							        	 .actionGet();
		
		return response.getHits();

	}
	
	public HashMap<String, JSONObject> getFieldsStatisticsOnCollection(){
		String[] fieldsArr = new String[fields.size()];
		fieldsArr = fields.toArray(fieldsArr);
		
		HashMap<String, JSONObject> statistics = new HashMap<String, JSONObject>();
		
		
		FieldStatsResponse response = new FieldStatsRequestBuilder(client, FieldStatsAction.INSTANCE )
							.setIndices(indexName)
							.setFields(fieldsArr)
							//.setFields("all")
							.execute()
							.actionGet();
		
		for(Map.Entry<String, FieldStats> entry : response.getAllFieldStats().entrySet()){
			String fieldName = entry.getKey();
			FieldStats fieldStats = entry.getValue();
			//System.out.println(fieldName +" - "+fieldStats.getSumTotalTermFreq()+" - "+fieldStats.getDocCount());
			JSONObject json = new JSONObject(
							"{sum_ttf:"+fieldStats.getSumTotalTermFreq()+
							", avg_length:"+((float)fieldStats.getSumTotalTermFreq()/fieldStats.getDocCount())+
							", doc_count:"+fieldStats.getDocCount()+
							"}"
							);
			statistics.put(fieldName, json);
		}
		
		return statistics;
	}
	
	public HashMap<String, ArrayList<Integer>> getQueryTermsPositionsOnField(ArrayList<String> terms, String fieldContent){
		HashMap<String, ArrayList<Integer>> termsPositions = new HashMap<String, ArrayList<Integer>>();
		for(String term: terms)
			termsPositions.put(term, new ArrayList<Integer>());
		
		StandardAnalyzer analyzer = new StandardAnalyzer();
		int pos = 0;
		try {
			TokenStream tokenStream = analyzer.tokenStream("default", fieldContent);
			tokenStream.reset();
			CharTermAttribute term = tokenStream.getAttribute(CharTermAttribute.class);
			while(tokenStream.incrementToken()){
				pos++;
		        if(terms.contains(term.toString())){
		        	termsPositions.get(term.toString()).add(pos);
		        	//System.out.println(term+" - "+pos);
		        }
			}
			tokenStream.end();
			tokenStream.close();
			
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		analyzer.close();
		
		
		return termsPositions;
	}
	
	public int countWindowBigramsFrequency(String[] bigram, HashMap<String, ArrayList<Integer>> termsPositions){
		int window = 8;
		int tf = 0;
		if(termsPositions.get(bigram[0]) == null || termsPositions.get(bigram[1]) == null)
			return tf;
		
		for(int pos1 : termsPositions.get(bigram[0])){
			for(int pos2 : termsPositions.get(bigram[1])){
				if(Math.abs(pos1 - pos2) < window)
					tf++;
				//else
					//break;
			}
		}
		
		return tf;
	}
	
	public HashMap<String, Integer> getWindowBigramsFrequencyByField(ArrayList<String> uTerms, ArrayList<String> bTerms, SearchHit hit, String field){
		//first, get all terms positions for each field content
		HashMap<String, ArrayList<Integer>> termsPositions = getQueryTermsPositionsOnField(uTerms, hit.field(field).value().toString());
		
		HashMap<String, Integer> frequencies = new HashMap<String, Integer>(); 
		for(String term : bTerms){
			String[] bigram = term.split(" ");
			int tf = countWindowBigramsFrequency(bigram, termsPositions);
			frequencies.put(term, tf);
			//System.out.println(field+" - '"+term+"' - "+tf);
		}
		
		return frequencies;
	}
	
	public HashMap<String, HashMap<String, Integer>> getWindowBigramsFrequencies(ArrayList<String> uTerms, ArrayList<String> bTerms, SearchHit hit){
		HashMap<String, HashMap<String, Integer>> allTfs = new HashMap<String, HashMap<String, Integer>>();
		for(String field : fields){
			HashMap<String, Integer> tfs = getWindowBigramsFrequencyByField(uTerms, bTerms, hit, field);
			allTfs.put(field, tfs);
		}
		
		return allTfs;
	}
	
	public HashMap<String, Integer> getDocFieldsLengths(SearchHit hit){
		HashMap<String, Integer> fieldsLengths = new HashMap<String, Integer>();
		
		for(String field : fields)
			fieldsLengths.put(field, getDocLen(hit.field(field).value().toString()));
		
		return fieldsLengths;
	}
	
	public HashMap<String, String> getDocUnigramFrequencies(SearchHit hit){
		HashMap<String, String> uFreqs = new HashMap<String, String>();
		for(String field : fields)
			uFreqs.put(field, hit.field("tf_"+field).value().toString().replace("=", ":"));
		
		return uFreqs;
	}
	
	public HashMap<String, String> getDocBigramFrequencies(SearchHit hit){
		HashMap<String, String> uFreqs = new HashMap<String, String>();
		for(String field : fields) {
			
			if (hit.field("tf_"+field+".bigrams").value() != null) {
				uFreqs.put(field, hit.field("tf_"+field+".bigrams").value().toString().replace("=", ":"));
			}
		}
		return uFreqs;
	}
	
	public void saveJsonQueryResult(String queryId, String query, String folderPath){
		ArrayList<String> uTerms = tokenizeQuery(query, "standard");
		ArrayList<String> bTerms = tokenizeQuery(query, "bigram_analyzer");
		
		HashMap<String, JSONObject> fieldsStats = getFieldsStatisticsOnCollection();
		HashMap<String, JSONObject> uCollFreq = getTermsFrenqueciesOnCollection(uTerms);
		HashMap<String, JSONObject> bCollFreq = getBiTermsFrenqueciesOnCollection(bTerms);
		
		SearchHits hits = searchEntities(query, uTerms, bTerms, 5000);
		
		
		try (BufferedWriter writer = Files.newBufferedWriter(Paths.get(folderPath+"/"+queryId+".txt"))) {
			JSONObject jsonUnigrams = new JSONObject("{unigrams: "+uTerms.toString()+"}");
			JSONObject jsonBigrams = new JSONObject("{bigrams: "+bTerms.toString()+"}");
			JSONObject jsonFieldsStats = new JSONObject(fieldsStats.toString().replace("=", ":"));
			JSONObject jsonUCollFreq = new JSONObject(uCollFreq.toString().replace("=", ":"));
			JSONObject jsonBCollFreq = new JSONObject(bCollFreq.toString().replace("=", ":"));
			
			writer.write(jsonUnigrams.toString()+"\n");
			writer.write(jsonBigrams.toString()+"\n");
			writer.write(jsonFieldsStats.toString()+"\n");
			writer.write(jsonUCollFreq.toString()+"\n");
			writer.write(jsonBCollFreq.toString()+"\n");
			
			for(SearchHit hit : hits){
				
				//get window bigrams frequencies
				HashMap<String, HashMap<String, Integer>> tfs = getWindowBigramsFrequencies(uTerms, bTerms, hit);
				
				JSONObject doc = new JSONObject(
						"{"+
						"\"url\":\""+hit.field("docno").value()+"\","+
						"\"fields_lengths\":"+getDocFieldsLengths(hit).toString().replace("=", ":")+","+
						"\"unigrams_frequencies\":"+getDocUnigramFrequencies(hit).toString().replace("=", ":")+","+
						"\"bigrams_frequencies\":"+getDocBigramFrequencies(hit).toString().replace("=", ":")+","+
						"\"wbigrams_frequencies\":"+tfs.toString().replace("=", ":")+","+
						"}");
				
				writer.write(doc.toString()+"\n");
			}
        }
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processAndSaveAllQueryResults(String queriesFilePath, String folderPath){
		parseQueriesFile(queriesFilePath);
		int i = 1;
		for(Map.Entry<String, String> entry : queries.entrySet()){
			String queryId = entry.getKey();
			String query = entry.getValue();
			
			System.out.println("["+dateFormat.format(new Date())+"] "+i+" "+query);
			try{
				saveJsonQueryResult(queryId, query, folderPath);
			}
			catch(Exception e){
				System.out.println("["+dateFormat.format(new Date())+"] Erro na consulta: "+i+" "+query);
				e.printStackTrace();
			}
			i++;
		}
	}
}
