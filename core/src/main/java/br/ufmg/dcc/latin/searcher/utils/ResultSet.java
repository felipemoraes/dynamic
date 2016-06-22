/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;

import br.ufmg.dcc.latin.searcher.AdHocSearcher;
import br.ufmg.dcc.latin.searcher.WeightingModule;
import br.ufmg.dcc.latin.searcher.models.*;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

/**
 * @author Felipe Moraes
 *
 */
public class FatResultSet {
	
	static Logger log = Logger.getLogger("dynamic");
	private HashMap<String,WeightingModel> models;
	// topicId, docId, fatSet
	private HashMap<String, Set<String>> initialRanking;
	
	
	private String initialModel;

	/**
	 * 
	 */
	public FatResultSet(String initialModel) {
		
		
		
		String[] independences = {"standardized", "saturated", "chisquared"};
		String[] distributions  = {"ll", "spl"};
		String[] lambdas = {"df", "ttf"};
		String[] basicModels = {"be", "d", "g", "if", "in", "ine","p"};
		String[] afterEffects = {"no", "b" ,"l"};
		String[] normalizations = {"no", "h1", "h2", "h3", "z"};
		
		models = new HashMap<String,WeightingModel>();
		models.put("TFIDF", new Default());
		models.put("LMDirichlet", new LMDirichlet(2500.0));
		models.put("LMJelinekMercer", new LMJelinekMercer(0.25));
		models.put("BM25", new BM25(0.75,1.2));
		for (String independence : independences) {
			models.put("DFI_" + independence, new DFI(independence));
		}		
		for (String basicModel : basicModels) {
			for (String afterEffect : afterEffects) {
				for (String normalization : normalizations) {
					models.put("DFR_" + basicModel + "_" 
							+ afterEffect + "_" + normalization, new DFR(basicModel,afterEffect,normalization));
				}
			}
		}
		
		for (String lambda : lambdas) {
			for (String distribution : distributions) {
				for (String normalization : normalizations) {
					models.put("IB_" + distribution + "_" + lambda + "_" + normalization,
							new IB(distribution,lambda,normalization));
				}
			}
		}
		this.initialModel = initialModel;
		initialRanking = new HashMap<String, Set<String>>();
		
	}

	public void build(List<QueryInfo> queries) throws UnknownHostException{
		log.info("Start initial Ranking generation");
		Set<String> indicesName = new HashSet<String>();
		for (QueryInfo query : queries) {
			indicesName.add(query.getIndexName());
		}
		
		//WeightingModule weightingModule = new WeightingModule();
		//weightingModule.changeWeightingModel(indicesName, models.get(initialModel));
		AdHocSearcher adHocSearcher = new AdHocSearcher();
		CollectionCandidates collection = new CollectionCandidates();
		for (QueryInfo queryInfo : queries) {
			System.out.println(queryInfo.getId());
			Map<String, Double> resultSet = adHocSearcher
					.initialSearch(queryInfo.getIndexName(), queryInfo.getText(), 5);
			initialRanking.put(queryInfo.getId(),resultSet.keySet());
			
			for (String docId : resultSet.keySet()) {
				try {
					System.out.println(queryInfo.getIndexName() +  " text " +  " doc " +  docId);
					collection.putDocCandidate(queryInfo.getIndexName(), "text", "doc", docId);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/*try {
				BufferedWriter out = new BufferedWriter(new FileWriter(queryInfo.getId()));
				for (Entry<String, Double> entry : resultSet.entrySet()) {
					out.write(entry.getKey() + " " + initialModel +" " +entry.getValue() + "\n");
				}
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
		}
		
		
	
		//expand(queries, indicesName);
	}
	
	private void expand(List<QueryInfo> queries, Set<String> indicesName)
			throws UnknownHostException {
		
		AdHocSearcher adHocSearcher = new AdHocSearcher();
		WeightingModule weightingModule = new WeightingModule();
		/*
		for (Entry<String, WeightingModel> model : models.entrySet() ){
			if (model.getKey().equals(initialModel)) {
				continue;
			}
			weightingModule.changeWeightingModel(indicesName, model.getValue());
			log.info("Processing " + model.getKey());
			
			for (QueryInfo queryInfo : queries) {
				
				Map<String,Double> resultSet = adHocSearcher
						.searchAndFilter(queryInfo.getIndexName(), 
								queryInfo.getText(), initialRanking.get(queryInfo.getId()));
				System.out.println(resultSet.size());
				try {
					BufferedWriter out;
					out = new BufferedWriter(new FileWriter(queryInfo.getId(),true));
					for (Entry<String, Double> entry : resultSet.entrySet()) {
						out.write(entry.getKey() + " " + model.getKey() +" " +entry.getValue() + "\n");
					}
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			
			}
		}
		*/

		weightingModule.changeWeightingModel(indicesName, models.get("BM25"));
		
		for (QueryInfo queryInfo : queries) {
			BufferedWriter out;
			try {
				out = new BufferedWriter(new FileWriter(queryInfo.getId()+"_details"));

				HashMap<String,Details> details = adHocSearcher.
						searchDetails(queryInfo.getIndexName(), queryInfo.getText(), 
						initialRanking.get(queryInfo.getId()), models.get("BM25"));
				for (Entry<String, Details> docDetails : details.entrySet()) {
					for (Entry<String, TermDetails> termsDetails : docDetails.getValue().getTerms().entrySet()) {
						for (Entry<String, Double>  termValue: termsDetails.getValue().getTermDetails().entrySet()) {
							out.write(docDetails.getKey() + " BM25 " + 
								termsDetails.getKey() +  " " + termValue.getKey() +" "+ termValue.getValue() + "\n" );
						}
						
					}
				}
				if (details != null){
					computeDetailsBM25(queryInfo,details);
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		weightingModule.changeWeightingModel(indicesName, models.get("LMDirichlet"));
		
		for (QueryInfo queryInfo : queries) {
			BufferedWriter out;
			try {
				out = new BufferedWriter(new FileWriter(queryInfo.getId()+"_details"));

				HashMap<String,Details> details = adHocSearcher.
						searchDetails(queryInfo.getIndexName(), queryInfo.getText(), 
						initialRanking.get(queryInfo.getId()), models.get("LMDirichlet"));
				for (Entry<String, Details> docDetails : details.entrySet()) {
					for (Entry<String, TermDetails> termsDetails : docDetails.getValue().getTerms().entrySet()) {
						for (Entry<String, Double>  termValue: termsDetails.getValue().getTermDetails().entrySet()) {
							out.write(docDetails.getKey() + " LMDirichlet " + 
								termsDetails.getKey() +  " " + termValue.getKey() +" "+ termValue.getValue() + "\n" );
						}
						
					}
				}
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}
	
	
	public Map<String,Double> getTermFrequency(HashMap<String, Details> details){
		Map<String,Double> resultSet = new HashMap<String,Double>();
		for (Entry<String, Details> propertyDetails : details.entrySet()) {
			Double score = 0.0;
			Integer count = 0;
			for (Entry<String, TermDetails> termDetails : propertyDetails.getValue().getTerms().entrySet()) {
				for (Entry<String, Double> term : termDetails.getValue().getTermDetails().entrySet()) {
					if (termDetails.getKey().equals("TF")) {
						score += term.getValue();
						count++;
					}
					
				}
				
			}
			if (count > 0 && score > 0) {
				resultSet.put(propertyDetails.getKey(), score/count);
			} else {
				resultSet.put(propertyDetails.getKey(), 0.0);
			}
		}
		return resultSet;
	}
	
	public Map<String,Double> getInverseDocFrequency(HashMap<String, Details> details){
		Map<String,Double> resultSet = new HashMap<String,Double>();
		for (Entry<String, Details> propertyDetails : details.entrySet()) {
			Double score = 0.0;
			Integer count = 0;
			for (Entry<String, TermDetails> termDetails : propertyDetails.getValue().getTerms().entrySet()) {
				for (Entry<String, Double> term : termDetails.getValue().getTermDetails().entrySet()) {
					if (termDetails.getKey().equals("IDF")) {
						score += term.getValue();
						count++;
					}
					
				}
				
			}
			if (count > 0 && score > 0) {
				resultSet.put(propertyDetails.getKey(), score/count);
			} else {
				resultSet.put(propertyDetails.getKey(), 0.0);
			}
		}
		return resultSet;
		
	}
	
	public Map<String,Double> getDocLength(HashMap<String, Details> details){
		Map<String,Double> resultSet = new HashMap<String,Double>();
		for (Entry<String, Details> propertyDetails : details.entrySet()) {
			Double score = 0.0;
			Integer count = 0;
			for (Entry<String, TermDetails> termDetails : propertyDetails.getValue().getTerms().entrySet()) {
				for (Entry<String, Double> term : termDetails.getValue().getTermDetails().entrySet()) {
					if (termDetails.getKey().equals("DL")) {
						score += term.getValue();
						count++;
					}
				}
				
			}
			if (count > 0 && score > 0) {
				resultSet.put(propertyDetails.getKey(), score/count);
			} else {
				resultSet.put(propertyDetails.getKey(), 0.0);
			}
		}
		return resultSet;
		
	}
	
	

	/**
	 * 
	 */
	private void computeDetailsBM25(QueryInfo queryInfo, HashMap<String,Details> details) {
		Map<String,Double> resultSet = getTermFrequency(details);
			
		try {
			BufferedWriter out;
			out = new BufferedWriter(new FileWriter(queryInfo.getId(),true));
			for (Entry<String, Double> entry : resultSet.entrySet()) {
				out.write(entry.getKey() + " TF " +  +entry.getValue() + "\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		resultSet = getInverseDocFrequency(details);
		try {
			BufferedWriter out;
			out = new BufferedWriter(new FileWriter(queryInfo.getId(),true));
			for (Entry<String, Double> entry : resultSet.entrySet()) {
				out.write(entry.getKey() + " IDF " +  +entry.getValue() + "\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		resultSet = getDocLength(details);
		try {
			BufferedWriter out;
			out = new BufferedWriter(new FileWriter(queryInfo.getId(),true));
			for (Entry<String, Double> entry : resultSet.entrySet()) {
				out.write(entry.getKey() + " DL " +  +entry.getValue() + "\n");
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	/*
	public void dump(){
		Integer counter = 0;
		try {
			log.info("Dumping to ES");
			Settings settings = Settings.settingsBuilder()
    			.put("cluster.name", "latin_elasticsearch").build();
        	Client client;
			client = TransportClient.builder().settings(settings).build().
			        addTransportAddress(new InetSocketTransportAddress(
			           InetAddress.getByName("localhost"), 9300));
			BulkRequestBuilder bulkRequest = client.prepareBulk();
			
			for (Entry<String, HashMap<String, FatSet>> queryFatResultSet : fatResultSet.entrySet()) {
				for (Entry<String, FatSet> fatSet : queryFatResultSet.getValue().entrySet()) {
					XContentBuilder jsonBuilder = jsonBuilder().startObject();
					
					for (Entry<String,Double> properties : fatSet.getValue().getModels().entrySet()) {
						jsonBuilder.field(properties.getKey(),properties.getValue());
					}
					jsonBuilder.field("topic_id", queryFatResultSet.getKey());
					jsonBuilder.field("doc_id", fatSet.getKey());

					
					jsonBuilder.field("BM25 TF", fatSetDetails.get(queryFatResultSet.getKey())
							.get("BM25").getDocsDetails().get(fatSet.getKey()).getPropertyDetails().get("TF").getTermDetails());
					jsonBuilder.field("BM25 IDF", fatSetDetails.get(queryFatResultSet.getKey())
							.get("BM25").getDocsDetails().get(fatSet.getKey()).getPropertyDetails().get("IDF").getTermDetails());

					jsonBuilder.field("LMDirichlet Document norm",  fatSetDetails.get(queryFatResultSet.getKey())
							.get("LMDirichlet").getDocsDetails().get(fatSet.getKey()).getPropertyDetails()
							.get("Document norm").getTermDetails());
					
					jsonBuilder.field("LMDirichlet Term weight",  fatSetDetails.get(queryFatResultSet.getKey())
							.get("LMDirichlet").getDocsDetails().get(fatSet.getKey()).getPropertyDetails()
							.get("Term weight").getTermDetails());
					
					jsonBuilder.field("LMDirichlet Collection probability",  fatSetDetails.get(queryFatResultSet.getKey())
							.get("LMDirichlet").getDocsDetails().get(fatSet.getKey()).getPropertyDetails()
							.get("Collection probability").getTermDetails());
					
					jsonBuilder.endObject();
					bulkRequest.add(client.prepareIndex("cache_fatresultret", "fatset",
							queryFatResultSet.getKey()+"_"+ fatSet.getKey() )
							.setSource(jsonBuilder));
					counter++;
					if (counter > 500){
						bulkRequest.execute().actionGet();
						bulkRequest = client.prepareBulk();
					}
				}
			}
			bulkRequest.execute().actionGet();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}*/
	
	
	public HashMap<String,WeightingModel> getModels() {
		return models;
	}

	public void setModels(HashMap<String,WeightingModel> models) {
		this.models = models;
	}

	public String getInitialModel() {
		return initialModel;
	}

	public void setInitialModel(String initialModel) {
		this.initialModel = initialModel;
	}





}
