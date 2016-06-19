/**
 * 
 */
package br.ufmg.dcc.latin.evaluation.experiments;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity.DefaultCollectionModel;
import org.apache.lucene.search.similarities.LMSimilarity.LMStats;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.fieldstats.FieldStatsResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentHelper;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.json.JSONObject;

import br.ufmg.dcc.latin.searcher.WeightingModule;
import br.ufmg.dcc.latin.searcher.models.BM25;
import br.ufmg.dcc.latin.searcher.models.LMDirichlet;
import br.ufmg.dcc.latin.searcher.scores.LMDirichletScorer;
import br.ufmg.dcc.latin.searcher.utils.CollectionCandidates;
import br.ufmg.dcc.latin.searcher.utils.TermStats;


/**
 * @author Felipe Moraes
 *
 */
public class Test {

	

	/**
	 * @throws IOException 
	 * 
	 */
	public static void main(String[] args) throws IOException {
		
		
    	Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch").build();
        Client client;
		
			client = TransportClient.builder().settings(settings).build().
			        addTransportAddress(new InetSocketTransportAddress(
			           InetAddress.getByName("localhost"), 9300));
			
			TermVectorsResponse resp = client.prepareTermVectors().setIndex("local_politics_2015")
					.setId("1325506560-3cd6d5303c6492f1b5484c4caa8139a6")
					.setType("doc")
					.setDfs(true)
					.setSelectedFields("text")
					.setPayloads(false)
					.setPositions(false)
					.setOffsets(false)
					.setFieldStatistics(true)
					.execute().actionGet();
					
			//TermVectorsResponse resp = client.prepareTermVectors("local_politics_2015", "doc", "1325506560-3cd6d5303c6492f1b5484c4caa8139a6")
		   // 		.setTermStatistics(true).setFieldStatistics(true)
		   // 		.setPayloads(false).setOffsets(false).setPositions(false).setDfs(true)
		   // 		.setSelectedFields("text").execute().actionGet();
			
			

			
		WeightingModule weightingModule = new WeightingModule();
		weightingModule.changeWeightingModel("twitter", new BM25(0.75,1.2));
		
		
        SearchResponse response = client.prepareSearch("twitter")
                .setTypes("tweet")
                .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
                .setQuery(QueryBuilders.queryStringQuery("test").field("text"))                 
                .setFrom(0).setSize(2)
                .setExplain(true)
                .execute()
                .actionGet();
        
        CollectionCandidates collection = new CollectionCandidates();
        for (SearchHit hit : response.getHits()) {
        	System.out.println(hit.getExplanation());
        	System.out.println(hit.getId() + " " + hit.getScore());
        	
        	collection.putDocCandidate("twitter", "text", "tweet", hit.getId());
        	
		}	
        
        AnalyzeRequest request = new AnalyzeRequest().text("test").analyzer("standard");
        List<AnalyzeResponse.AnalyzeToken> tokens = client.admin().indices().analyze(request).actionGet().getTokens();
        for (AnalyzeResponse.AnalyzeToken token : tokens) {
            System.out.println("token: " + token.getTerm());
        }
        
		BasicStats basicStats = new BasicStats("text");
		basicStats.setNumberOfDocuments(collection.getDocCount("twitter", "text"));
		basicStats.setNumberOfFieldTokens(collection.getSumTotalTermFreq("twitter", "text"));
		basicStats.setDocFreq(collection.getDocFreq("twitter", "text", "test"));
		basicStats.setTotalTermFreq(collection.getTotalTermFreq("twitter", "text", "test"));
		basicStats.setAvgFieldLength(basicStats.getNumberOfFieldTokens()/basicStats.getNumberOfDocuments());
		LMStats lmStats = new LMStats("text");
		float collectionProbability = (basicStats.getTotalTermFreq() + 1F) / (basicStats.getNumberOfFieldTokens()+ 1F);
		float mu = (float) 2500.0;
		lmStats.setCollectionProbability(collectionProbability);
		lmStats.setNumberOfDocuments(basicStats.getNumberOfDocuments());
		lmStats.setNumberOfFieldTokens(basicStats.getNumberOfFieldTokens());
		lmStats.setDocFreq(basicStats.getDocFreq());
		lmStats.setTotalTermFreq(basicStats.getTotalTermFreq());
		lmStats.setAvgFieldLength(basicStats.getAvgFieldLength());
		LMDirichletScorer lmScorer = new LMDirichletScorer((float) 2500.0);
		
		System.out.println(collection.getDocFreq("twitter", "text", "test"));
		System.out.println(collection.getTotalTermFreq("twitter", "text", "test"));
		
		System.out.println("collection probability " + collectionProbability);
		System.out.println("My scorer " + lmScorer.totalScore(lmStats, 
				 collection.getTermFreq("twitter", "text","2", "test"), collection.getDocLen("twitter", "text","2") ));
		System.out.println((float)Math.log(mu /(collection.getDocLen("twitter", "text","2") +mu)) +  " document norm");
		
		
		System.out.println((float)Math.log(1 + 
				collection.getTermFreq("twitter", "text","2", "test")/(mu * (lmStats.getCollectionProbability()))) +  " term weight" );
		
		client.close();

	}

}
