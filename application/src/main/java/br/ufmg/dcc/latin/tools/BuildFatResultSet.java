/**
 * 
 */
package br.ufmg.dcc.latin.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
		
		String topicsFile = "src/main/resources/sample_topics_domain.txt";
		List<QueryInfo> queryInfos = new ArrayList<QueryInfo>();
		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    while ((line = br.readLine()) != null) {

		    	String[] splitLine = line.split(" ",3);
	        	String indexName = splitLine[0];
	        	if (indexName.equals("Ebola")) {
	        		indexName = "ebola_2015";
				} else if (indexName.equals("Local_Politics")){
					indexName = "local_politics";
				} else if (indexName.equals("Illicit_Goods")){
					indexName = "illicit_goods";
				}
	        	
	        	String topicId = splitLine[1];
	    		String query = splitLine[2];
	    		
	    		QueryInfo queryInfo = new QueryInfo();
	    		queryInfo.setId(topicId);
	    		queryInfo.setIndexName(indexName);
	    		queryInfo.setText(query);
	    		queryInfos.add(queryInfo);
		    }
		    
		    FatResultSet fatResultSet = new FatResultSet();
		    fatResultSet.build(queryInfos);
		    fatResultSet.dump();
		    
		} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
		}

	}

}
