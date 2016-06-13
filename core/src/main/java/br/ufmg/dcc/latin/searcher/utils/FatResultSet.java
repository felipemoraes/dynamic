/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;

import br.ufmg.dcc.latin.searcher.AdHocSearcherFactory;
import br.ufmg.dcc.latin.searcher.Searcher;
import br.ufmg.dcc.latin.searcher.models.*;

import static org.elasticsearch.common.xcontent.XContentFactory.*;

/**
 * @author Felipe Moraes
 *
 */
public class FatResultSet {
	
	private HashMap<String,WeightingModel> models;
	// topicId, docId, fatSet
	private HashMap<String,  HashMap<String, FatSet > > fatResultSet;
	
	// model, details 
	private HashMap< String, Details> fatSetFetails;
	
	private String initialModel;

	/**
	 * 
	 */
	public FatResultSet() {
		
		String[] independences = {"standardized", "saturated", "chisquared"};
		String[] distributions  = {"ll", "spl"};
		String[] lambdas = {"df", "ttf"};
		String[] basicModels = {"be", "d", "g", "if", "in", "ine","p"};
		String[] afterEffects = {"no", "b" ,"l"};
		String[] normalizations = {"no", "h1", "h2", "h3", "z"};
		
		models = new HashMap<String,WeightingModel>();
		fatSetFetails = new HashMap< String, Details>();
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

		initialModel = "LMDirichlet";
		System.out.println("Initiate all models.");
		fatResultSet = new HashMap<String, HashMap<String, FatSet> >();
		
	}

	public void build(List<QueryInfo> queries) throws UnknownHostException{
		System.out.println("Started initial ranking.");

		AdHocSearcherFactory adHocSearcherFactory = new AdHocSearcherFactory(models.get(initialModel));
		for (QueryInfo queryInfo : queries) {
			Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(queryInfo.getIndexName());
			ResultSet resultSet = null;
			if (initialModel == "BM25" || initialModel == "LMDirichlet" ) {
				resultSet = adHocSearcher.search(queryInfo.getText(), 5, models.get(initialModel));
				if (resultSet.getDetails() != null){
					fatSetFetails.put(initialModel, resultSet.getDetails());
				}
			} else {
				resultSet = adHocSearcher.search(queryInfo.getText(), 5);
			}
			
			HashMap<String, FatSet > initialRanking = new HashMap<String, FatSet >();
			for (Entry<String, Double> result : resultSet.getResultSet().entrySet()) {
				FatSet tempSet = new FatSet();
				tempSet.getModels().put(initialModel, result.getValue());
				initialRanking.put(result.getKey(), tempSet);
			}
			fatResultSet.put(queryInfo.getId(),initialRanking);
		}
		System.out.println("Ended initial ranking.");
		
		for (Entry<String, FatSet> doc: fatResultSet.get("DD15-49").entrySet()) {
			System.out.println(doc.getKey() + " - " + doc.getValue().toString());
		}
		
		
	
		expand(queries);
	}

	/**
	 * 
	 */
	private void expand(List<QueryInfo> queries)
			throws UnknownHostException {
		System.out.println("Expanding...");
		AdHocSearcherFactory adHocSearcherFactory;
		
		for (Entry<String, WeightingModel> model : models.entrySet() ){
			System.out.println("Model: " + model.getKey());
			adHocSearcherFactory = new AdHocSearcherFactory(model.getValue());
			if (!model.getKey().equals(initialModel) && 
					(model.getKey().equals("BM25") || model.getKey().equals("LMDirichlet") )){
				for (QueryInfo queryInfo : queries) {
					Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(queryInfo.getIndexName());
					ResultSet resultSet = adHocSearcher.search(queryInfo.getText(), 
							fatResultSet.get(queryInfo.getId()).keySet(), model.getValue());
					if (resultSet.getDetails() != null){
						fatSetFetails.put(model.getKey(), resultSet.getDetails());
					}
					for (Entry<String, Double> result : resultSet.getResultSet().entrySet()) {
						fatResultSet.get(queryInfo.getId()).get(result.getKey()).getModels().put(model.getKey(), result.getValue());
					}
				}
			} else {
				
				for (QueryInfo queryInfo : queries) {
					Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(queryInfo.getIndexName());
					ResultSet resultSet = adHocSearcher.search(queryInfo.getText(), 
							fatResultSet.get(queryInfo.getId()).keySet());
					System.out.println(resultSet.getResultSet().size());
					for (Entry<String, Double> result : resultSet.getResultSet().entrySet()) {
						fatResultSet.get(queryInfo.getId()).get(result.getKey()).getModels().put(model.getKey(), result.getValue());
					}
				}
				
			} 
			
		}
	}
	
	public void dump(){
		Integer counter = 0;
		try {
			System.out.println("Dumping");
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
					jsonBuilder.endObject();
					bulkRequest.add(client.prepareIndex("cache_fatresultret", "fatset",
							queryFatResultSet.getKey()+"_"+ fatSet.getKey() )
							.setSource(jsonBuilder));
					counter++;
					if (counter > 500){
						BulkResponse response = bulkRequest.execute().actionGet();
						bulkRequest = client.prepareBulk();
						System.out.println(response.buildFailureMessage());
					}
				}
			}
			BulkResponse response  = bulkRequest.execute().actionGet();
			System.out.println(response.buildFailureMessage());
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
	}
	
	
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



	public HashMap< String, Details> getFatSetFetails() {
		return fatSetFetails;
	}

	public void setFatSetFetails(HashMap< String, Details> fatSetFetails) {
		this.fatSetFetails = fatSetFetails;
	}


}
