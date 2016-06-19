/**
 * 
 */
package br.ufmg.dcc.latin.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import br.ufmg.dcc.latin.searcher.utils.FatResultSet;
import br.ufmg.dcc.latin.searcher.utils.QueryInfo;

/**
 * @author Felipe Moraes
 *
 */
public class BuildFatResultSet {

	/**
	 * 
	 */
	public static void main(String[] args) {
		
		
		Options options = new Options();
		options.addOption("t", "topic", true, "Topics file.");
		options.addOption("r", "ranking", true, "Initial ranking model.");
		CommandLine cmd = null;
		CommandLineParser parser = new DefaultParser();
		String topicsFile = "src/main/resources/topics_domain.txt";
		String initialRanking = "LMDirichlet";
		try {
			cmd = parser.parse(options, args);
			if (cmd.hasOption("t")) {
				topicsFile = cmd.getOptionValue("t");
			}
			if (cmd.hasOption("i")) {
				initialRanking = cmd.getOptionValue("i");
			}
		} catch (ParseException e) {
			System.err.println("Failed to parse comand line properties");
			System.exit(0);
		}
		
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
		    
		    FatResultSet fatResultSet = new FatResultSet(initialRanking);
		    fatResultSet.build(queryInfos);
		   
		    
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}

	}

}
