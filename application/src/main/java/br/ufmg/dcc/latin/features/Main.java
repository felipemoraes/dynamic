/**
 * 
 */
package br.ufmg.dcc.latin.features;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.SearchService;


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
		
		
		/*
		try {
			WeightingModule.changeWeightingModel(ApplicationSetup.ES_INDEX_NAME, ApplicationSetup.INITIAL_RANKING_MODEL);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
		ConfigService configService = new ConfigService();
		configService.config(configFilePath);
		
		float startTime = System.nanoTime();
		
		SearchService searchService = new SearchService(Config.ES_INDEX_NAME, Config.ES_INDEX_TYPE,"doc");
		

		// QueryIndependentFeatures queryIndependentFeatures;
		
		FeaturesService featuresService = new FeaturesService();
		//LETOROutputFormat letorOutputFormat = new LETOROutputFormat(Config.OUTPUT_FILENAME);
		
		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ",2);
	        	int queryId = Integer.parseInt(splitLine[0]);
	    		String query = splitLine[1];
	    		
	    		ResultSet resultSet = searchService.search(query, 1000);
	    		
	    		FeaturedResultSet featuredResultSet = new FeaturedResultSet(resultSet,featuresService);
	    		
	    		//letorOutputFormat.write(queryId, featuredResultSet.getScores(), featuredResultSet.getDocIds());
	    		
		    }
		    
		    
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}
		
		float estimatedTime = System.nanoTime() - startTime;
		System.out.println("Elapsed time: " + estimatedTime/1000000000 );

	}

}
