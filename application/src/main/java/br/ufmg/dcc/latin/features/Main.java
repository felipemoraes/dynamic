/**
 * 
 */
package br.ufmg.dcc.latin.features;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.SearchService;
import br.ufmg.dcc.latin.searcher.WeightingModule;



/**
 * @author Felipe Moraes
 *
 */
public class Main {

	/**
	 * 
	 */
	public static void main(String[] args) {
		
		
		Options options = new Options();
		options.addOption("t", "topic", true, "Topics file.");
		options.addOption("c", "config", true, "Configuration file.");
		CommandLine cmd = null;
		CommandLineParser parser = new DefaultParser();
		String topicsFile = "src/main/resources/sample_topics_domain.txt";
		String configFilePath = "src/main/resources/config";
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("t")) {
				topicsFile = cmd.getOptionValue("t");
			}
			if (cmd.hasOption("c")) {
				configFilePath = cmd.getOptionValue("c");
			}
		} catch (ParseException e) {
			System.err.println("Failed to parse comand line properties");
			System.exit(0);
		}
		

		
		ConfigService configService = new ConfigService();
		configService.config(configFilePath);
		
		
		
		try {
			WeightingModule.changeWeightingModel(Config.ES_INDEX_NAME, Config.INITIAL_RANKING_MODEL);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	
		
		float startTime = System.nanoTime();
		
		SearchService searchService = new SearchService(Config.ES_INDEX_NAME, "text", Config.ES_DOC_TYPE);
		

		// QueryIndependentFeatures queryIndependentFeatures;
		System.out.println(System.getProperty("user.dir"));
		ArrayList<String> queryDependentFeatures = new ArrayList<String>();
		try {
			
			Scanner scanner = new Scanner(new BufferedReader(new FileReader(Config.QUERY_DEPENDENT_FILENAME)));
			while (scanner.hasNextLine()) {
				queryDependentFeatures.add(scanner.nextLine());
			   
			}
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		
		FeaturesService featuresService = new FeaturesService(queryDependentFeatures);
		LETOROutputFormat letorOutputFormat = new LETOROutputFormat(Config.OUTPUT_FILENAME);
		
		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ",2);
	        	int queryId = Integer.parseInt(splitLine[0]);
	    		String query = splitLine[1];
	    		System.out.println(queryId);

	    		ResultSet resultSet = searchService.search(query, Config.ES_FIELDS, 1);
	    		
	    		FeaturedResultSet featuredResultSet = new FeaturedResultSet(resultSet,featuresService, Config.ES_FIELDS);
	    		
	    		letorOutputFormat.write(queryId, featuredResultSet.getScores(), resultSet.getDocNos());
	    		 	
		    }
		    
		    
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		letorOutputFormat.close();
		float estimatedTime = System.nanoTime() - startTime;
		System.out.println("Elapsed time: " + estimatedTime/1000000000 );

	}

}
