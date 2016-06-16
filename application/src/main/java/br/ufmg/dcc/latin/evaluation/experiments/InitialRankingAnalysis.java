package br.ufmg.dcc.latin.evaluation.experiments;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import br.ufmg.dcc.latin.searcher.AdHocSearcher;
import br.ufmg.dcc.latin.searcher.WeightingModule;
import br.ufmg.dcc.latin.searcher.models.BM25;
import br.ufmg.dcc.latin.searcher.models.DFI;
import br.ufmg.dcc.latin.searcher.models.DFR;
import br.ufmg.dcc.latin.searcher.models.Default;
import br.ufmg.dcc.latin.searcher.models.IB;
import br.ufmg.dcc.latin.searcher.models.LMDirichlet;
import br.ufmg.dcc.latin.searcher.models.LMJelinekMercer;
import br.ufmg.dcc.latin.searcher.models.WeightingModel;
import br.ufmg.dcc.latin.searcher.utils.QueryInfo;
import br.ufmg.dcc.latin.searcher.utils.ResultSet;

public class InitialRankingAnalysis {

	public static void main(String[] args) {
		
		String[] independences = {"standardized", "saturated", "chisquared"};
		String[] distributions  = {"ll", "spl"};
		String[] lambdas = {"df", "ttf"};
		String[] basicModels = {"be", "d", "g", "if", "in", "ine","p"};
		String[] afterEffects = {"no", "b" ,"l"};
		String[] normalizations = {"no", "h1", "h2", "h3", "z"};
		HashMap<String,WeightingModel> models;
		models = new HashMap<String,WeightingModel>();
		
		String topicsFile = "src/main/resources/topics_domain.txt";
		
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
		
		AdHocSearcher adHocSearcher = new AdHocSearcher();
		WeightingModule weightingModule = new WeightingModule();
		
		
		List<QueryInfo> queryInfos = new ArrayList<QueryInfo>();
		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {

		    	String[] splitLine = line.split(" ",3);
	        	String indexName = splitLine[0];
	        	if (indexName.equals("Ebola")) {
	        		indexName = "ebola_2015";
				} else if (indexName.equals("Local_Politics")){
					indexName = "local_politics_2015";
				} else if (indexName.equals("Illicit_Goods")){
					indexName = "illicit_goods_2015";
				}
	        	
	        	String topicId = splitLine[1];
	    		String query = splitLine[2];
	    		
	    		QueryInfo queryInfo = new QueryInfo();
	    		queryInfo.setId(topicId);
	    		queryInfo.setIndexName(indexName);
	    		queryInfo.setText(query);
	    		queryInfos.add(queryInfo);
		    }
		    

		    
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		Set<String> indicesName = new HashSet<String>();
		for (QueryInfo query : queryInfos) {
			indicesName.add(query.getIndexName());
		}
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("initial_ranking.txt"));
		
		    for (Entry<String, WeightingModel> model : models.entrySet() ){
		    	if (!model.getKey().equals("LMDirichlet")) {
		    		continue;
		    	}
	    		long startTime = System.nanoTime();    
	    	
	    		System.out.println("Processing " + model.getKey());
	    		System.out.println("Changing " + model.getKey());
				weightingModule.changeWeightingModel(indicesName, model.getValue());
				long estimatedTime = System.nanoTime() - startTime;
				System.out.println("Elapsed time: " + estimatedTime/1000000000 );
				/*
				for (QueryInfo queryInfo : queryInfos) {
					ResultSet resultSet = adHocSearcher
							.initialSearch(queryInfo.getIndexName(), queryInfo.getText(), 10000);
					System.out.println(resultSet.getResultSet().size());
					for (Entry<String, Double> result : resultSet.getResultSet().entrySet()) {
				
						out.write(queryInfo.getId() + " " + model.getKey() + " "+ result.getKey() + " " + result.getValue() + "\n");
					}
	

				}*/
				estimatedTime = System.nanoTime() - startTime;
				System.out.println("Elapsed time: " + estimatedTime/1000000000 );
			} 
		    out.close();
		}catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		
		
	}

}
