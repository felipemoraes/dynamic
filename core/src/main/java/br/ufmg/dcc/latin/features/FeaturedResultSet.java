/**
 * 
 */
package br.ufmg.dcc.latin.features;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.index.Fields;
import org.apache.lucene.index.FilterLeafReader;
import org.apache.lucene.index.FilterLeafReader.FilterTermsEnum;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.similarities.AfterEffect;
import org.apache.lucene.search.similarities.AfterEffectB;
import org.apache.lucene.search.similarities.AfterEffectL;
import org.apache.lucene.search.similarities.BasicModel;
import org.apache.lucene.search.similarities.BasicModelBE;
import org.apache.lucene.search.similarities.BasicModelD;
import org.apache.lucene.search.similarities.BasicModelG;
import org.apache.lucene.search.similarities.BasicModelIF;
import org.apache.lucene.search.similarities.BasicModelIn;
import org.apache.lucene.search.similarities.BasicModelIne;
import org.apache.lucene.search.similarities.BasicModelP;
import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.Distribution;
import org.apache.lucene.search.similarities.DistributionLL;
import org.apache.lucene.search.similarities.DistributionSPL;
import org.apache.lucene.search.similarities.Independence;
import org.apache.lucene.search.similarities.IndependenceChiSquared;
import org.apache.lucene.search.similarities.IndependenceSaturated;
import org.apache.lucene.search.similarities.IndependenceStandardized;
import org.apache.lucene.search.similarities.Lambda;
import org.apache.lucene.search.similarities.LambdaDF;
import org.apache.lucene.search.similarities.LambdaTTF;
import org.apache.lucene.search.similarities.Normalization;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.search.similarities.NormalizationH2;
import org.apache.lucene.search.similarities.NormalizationH3;
import org.apache.lucene.search.similarities.NormalizationZ;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.CharsRefBuilder;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeRequest;
import org.elasticsearch.action.admin.indices.analyze.AnalyzeResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsItemResponse;
import org.elasticsearch.action.termvectors.MultiTermVectorsRequestBuilder;
import org.elasticsearch.action.termvectors.MultiTermVectorsResponse;
import org.elasticsearch.action.termvectors.TermVectorsRequest.FilterSettings;
import org.elasticsearch.action.termvectors.TermVectorsResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.lucene.index.FilterableTermsEnum;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;

import br.ufmg.dcc.latin.searcher.AdHocSearcher;
import br.ufmg.dcc.latin.searcher.WeightingModule;
import br.ufmg.dcc.latin.searcher.matching.BM25Scorer;
import br.ufmg.dcc.latin.searcher.matching.DFIScorer;
import br.ufmg.dcc.latin.searcher.matching.DFRScorer;
import br.ufmg.dcc.latin.searcher.matching.IBScorer;
import br.ufmg.dcc.latin.searcher.matching.LMDirichletScorer;
import br.ufmg.dcc.latin.searcher.matching.LMJelinekMercerScorer;
import br.ufmg.dcc.latin.searcher.matching.Scorer;
import br.ufmg.dcc.latin.searcher.matching.TFIDFScorer;



/**
 * @author Felipe Moraes
 *
 */


public class FeaturedResultSet {
	
	
	static Logger logger = LoggerFactory.getLogger(FeaturedResultSet.class);
	private Map<String, Scorer> scorers;
	private QIFeaturesBuilder queryIndependentFeatures;
	
	private LETOROutputFormat letorOutputFormat;
	
	private AdHocSearcher adHocSearcher = new AdHocSearcher();
	
	private static Client client;

	/**
	 * 
	 */
	public FeaturedResultSet() {

		this.queryIndependentFeatures = new QIFeaturesBuilder();
		
		adHocSearcher = new AdHocSearcher();
		
		letorOutputFormat = new LETOROutputFormat(); 
		
		
		try {
			WeightingModule.changeWeightingModel(ApplicationSetup.ES_INDEX_NAME, ApplicationSetup.INITIAL_RANKING_MODEL);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
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
		
		
		this.scorers = new HashMap<String,Scorer>();
		
		HashMap<String,Independence> independences = new HashMap<String,Independence>();
		independences.put("chisquared", new IndependenceChiSquared());
		independences.put("saturated", new IndependenceSaturated());
		independences.put("standardized", new IndependenceStandardized());
		
		HashMap<String,Distribution> distributions = new HashMap<String,Distribution>();
		distributions.put("ll", new DistributionLL());
		distributions.put("sll", new DistributionSPL());
		
		HashMap<String, Lambda> lambdas = new HashMap<String, Lambda>();
		lambdas.put("df", new LambdaDF());
		lambdas.put("ttf", new LambdaTTF());
		
		HashMap<String, BasicModel> basicModels = new HashMap<String, BasicModel>();
		basicModels.put("be", new BasicModelBE());
		basicModels.put("d", new BasicModelD());
		basicModels.put("g", new BasicModelG());
		basicModels.put("if", new BasicModelIF());
		basicModels.put("in", new BasicModelIn());
		basicModels.put("ine", new BasicModelIne());
		basicModels.put("p", new BasicModelP());
		
		HashMap<String, AfterEffect> afterEffects = new HashMap<String, AfterEffect>();
		
		afterEffects.put("no", new AfterEffect.NoAfterEffect() );
		afterEffects.put("b", new  AfterEffectB());
		afterEffects.put("l", new  AfterEffectL());
		
		HashMap<String, Normalization> normalizations = new HashMap<String, Normalization>();
		normalizations.put("no", new Normalization.NoNormalization());
		normalizations.put("h1", new NormalizationH1());
		normalizations.put("h2", new NormalizationH2());
		normalizations.put("h3", new NormalizationH3());
		normalizations.put("z", new NormalizationZ());
	
		
		scorers.put("BM25", new BM25Scorer());
		scorers.put("TFIDF", new TFIDFScorer());
		scorers.put("LMDirichlet", new LMDirichletScorer(2500));
		scorers.put("LMJelinekMercer", new LMJelinekMercerScorer( 0.1F));
		for (Entry<String,Independence> independence : independences.entrySet()) {
			scorers.put("DFI_" + independence.getKey(), new DFIScorer(independence.getValue()));
		}		
		for (Entry<String,BasicModel> basicModel : basicModels.entrySet()) {
			for (Entry<String,AfterEffect> afterEffect : afterEffects.entrySet()) {
				for (Entry<String,Normalization> normalization: normalizations.entrySet()) {
					scorers.put("DFR_" + basicModel.getKey() + "_" 
							+ afterEffect.getKey() + "_" + normalization.getKey(),
							new DFRScorer(basicModel.getValue(), afterEffect.getValue(), normalization.getValue()));
				}
			}
		}
		
		
		for (Entry<String,Lambda> lambda : lambdas.entrySet()) {
			for (Entry<String,Distribution> distribution: distributions.entrySet()) {
				for (Entry<String,Normalization> normalization: normalizations.entrySet()) {
					scorers.put("IB_" + distribution.getKey() + "_" + lambda.getKey() + "_" 
				    + normalization.getKey(), new IBScorer(distribution.getValue(), lambda.getValue(),
				    		normalization.getValue()));
				}
			}
		}
		
		//scorers.put("TF", 0F);
		//scorers.put("IDF", 0F);
		//scorers.put("DL", 0F);
	}
	
	
	
	private float[] getAllFeatures(String field, String[] queryTerms, TermVectorsResponse response){
		
		Arrays.sort(queryTerms);
		int n = queryTerms.length;
		BasicStats[] basicStats = new BasicStats[n];
		long[] termFreq = new long[n];
		long docLen = 0;
		float[] qiFeatures = new float[0];
		float[] scores = new float[0];
		int ct = 0;
		try {
			
			Fields theFields = response.getFields();
			
			Iterator<String> fieldIter = theFields.iterator();
			final CharsRefBuilder spare = new CharsRefBuilder();
			int curr = 0;
	        while (fieldIter.hasNext()) {
	        	 String fieldName = fieldIter.next();
	        	 
	        	 Terms curTerms = theFields.terms(fieldName);
	        	 docLen = curTerms.size();
		         long docCount = curTerms.getDocCount();
		         long sumTotalTermFrequencies = curTerms.getSumTotalTermFreq();
	        	 TermsEnum termIter = curTerms.iterator();
	        	 
	             for (int i = 0; i < curTerms.size(); i++) {
	            	 BytesRef term = termIter.next();
	            	 spare.copyUTF8Bytes(term);
	            	 String termStr = spare.toString();
	            	
	            	 if (queryTerms[curr].compareTo(termStr) == 0){
	            	
		            	BasicStats bs = new BasicStats(field);
			            
		             	bs.setDocFreq(termIter.docFreq());
	             		bs.setTotalTermFreq(termIter.totalTermFreq());
		             	bs.setNumberOfDocuments(docCount);
		             	bs.setNumberOfFieldTokens(sumTotalTermFrequencies);
		             	bs.setAvgFieldLength((float)bs.getNumberOfFieldTokens()/(float)bs.getNumberOfDocuments());
		             	basicStats[ct] = bs;
		             	
		             	PostingsEnum posEnum = termIter.postings(null, PostingsEnum.ALL);
		             	termFreq[ct] = (long) posEnum.freq();
		             	ct++;
	            		curr++;
	            		if (curr == n) {
	            			break;
	            		}
	             	} else if (queryTerms[curr].compareTo(termStr) < 0){
	             		curr++;
	             		if (curr == n) {
	             			break;
	             		}
	             		
	             		
	             		
	             		if (queryTerms[curr].compareTo(termStr) == 0) {
		            		
			            	BasicStats bs = new BasicStats(field);
				            
			             	bs.setDocFreq(termIter.docFreq());
		             		bs.setTotalTermFreq(termIter.totalTermFreq());
			             	bs.setNumberOfDocuments(docCount);
			             	bs.setNumberOfFieldTokens(sumTotalTermFrequencies);
			             	bs.setAvgFieldLength((float)bs.getNumberOfFieldTokens()/(float)bs.getNumberOfDocuments());
			             	basicStats[ct] = bs;
			             	
			             	PostingsEnum posEnum = termIter.postings(null, PostingsEnum.ALL);
			             	termFreq[ct] = (long) posEnum.freq();
			             	ct++;
		            		curr++;
		            		if (curr == n) {
		            			break;
		            		}
	             		}
	             	} 
	             
	             }	
	 			
	        }
	        
	        int docId = Integer.parseInt(response.getId());
	        qiFeatures = queryIndependentFeatures.getDocFeatures(docId);
			scores = new float[scorers.size()+qiFeatures.length];
			
			int i = 0;
			

			for (Entry<String, Scorer> scorer : scorers.entrySet()) { 
				float score = scorer.getValue()
						.totalScore(basicStats, termFreq, docLen, ct);
				scores[i] = score;
				i++;
			}
			
			for (int j = 0; j < qiFeatures.length; j++) {
				scores[i+j] = qiFeatures[j];
			}
			

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return scores;
	}
	

	private void computeFeatures(String field, int queryId,  String[] queryTerms, MultiTermVectorsRequestBuilder req){
		MultiTermVectorsResponse resp = req.execute().actionGet();
		
		for (MultiTermVectorsItemResponse response : resp.getResponses()) {
			TermVectorsResponse getResponse = response.getResponse();
			float[] features = getAllFeatures(field,queryTerms, getResponse);
			letorOutputFormat.write(queryId, features);
		}	

	}
	
	public void computeAllDocFeatures(String field, String[] queryTerms, int queryId, int[] docs) throws IOException{
		
		MultiTermVectorsRequestBuilder req = client.prepareMultiTermVectors();
		
		int count = 0;
		for (int i = 0; i < docs.length; i++) {
			count++;
			req.add( client.prepareTermVectors(ApplicationSetup.ES_INDEX_NAME, 
					ApplicationSetup.ES_INDEX_TYPE, Integer.toString(docs[i]))
    		.setTermStatistics(true).setFieldStatistics(true)
    		.setPayloads(false).setOffsets(false).setPositions(false).setDfs(false)
    		.setSelectedFields(field).request());
			if (count >= 1000) {
				computeFeatures(field, queryId, queryTerms, req);
				req = client.prepareMultiTermVectors();
				count = 0;
			}
		}
		if (count > 0) {
			computeFeatures(field,queryId, queryTerms, req);
		}
		
	}
	
	private String[] getQueryTerms(String query){
        AnalyzeRequest request = new AnalyzeRequest().text(query).analyzer("standard");
        List<AnalyzeResponse.AnalyzeToken> tokens = client.admin().indices().analyze(request).actionGet().getTokens();
        int n = tokens.size();
        String[] terms = new String[n];
        int i = 0;
        for (AnalyzeResponse.AnalyzeToken token : tokens){
        	terms[i] = token.getTerm();
        	i++;
        }
		return terms;
	}
	
	public void process(int queryId, String query) throws IOException{
			
		int[] resultSet = adHocSearcher
					.initialSearch(ApplicationSetup.ES_INDEX_NAME, query, 1);
		
		
		//String[] queryTerms = getQueryTerms(query);
			
		//computeAllDocFeatures("text",queryTerms, queryId, resultSet);
		

	}



}
