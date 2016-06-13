/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import br.ufmg.dcc.latin.searcher.AdHocSearcherFactory;
import br.ufmg.dcc.latin.searcher.Searcher;
import br.ufmg.dcc.latin.searcher.models.*;
/**
 * @author Felipe Moraes
 *
 */
public class FatResultSet {
	
	private HashMap<String,WeightingModel> models;
	
	private HashMap<String,  HashMap<String, FatSet > > fatResultSet;
	
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
		models.put("BM25", new BM25(0.75,1.2));
		models.put("TFIDF", new Default());
		models.put("LMDirichlet", new LMDirichlet(2500.0));
		models.put("LMJelinekMercer", new LMJelinekMercer(0.25));
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
			ResultSet resultSet = adHocSearcher.search(queryInfo.getText(), 5);
			System.out.println(queryInfo.getText() + " " + resultSet.getResultSet().size());
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
		adHocSearcherFactory = new AdHocSearcherFactory(models.get("BM25"));
		for (QueryInfo queryInfo : queries) {
			Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(queryInfo.getIndexName());
			ResultSet resultSet = adHocSearcher.search(queryInfo.getText(), fatResultSet.get(queryInfo.getId()).keySet(), models.get("BM25"));

		}
		/*
		for (Entry<String, WeightingModel> model : models.entrySet() ){
			System.out.println("Model: " + model.getKey());
			adHocSearcherFactory = new AdHocSearcherFactory(model.getValue());
			if (model.getKey().equals("BM25")){
				for (QueryInfo queryInfo : queries) {
					Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(queryInfo.getIndexName());
					ResultSet resultSet = adHocSearcher.search(queryInfo.getText(), 
						fatResultSet.get(queryInfo.getId()).keySet());
				}
				continue;
			} else if (model.getKey() != initialModel ){
				
				for (QueryInfo queryInfo : queries) {
					Searcher adHocSearcher = adHocSearcherFactory.getAdHocSearcher(queryInfo.getIndexName());
					ResultSet resultSet = adHocSearcher.search(queryInfo.getText(), 
							fatResultSet.get(queryInfo.getId()).keySet());
					for (Entry<String, Double> result : resultSet.getResultSet().entrySet()) {
						fatResultSet.get(queryInfo.getId()).get(result.getKey()).getModels().put(model.getKey(), result.getValue());
					}
				}
				
			} 
		}*/
		for (Entry<String, FatSet> doc: fatResultSet.get("DD15-49").entrySet()) {
			System.out.println(doc.getValue().toString());
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
}
