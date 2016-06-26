package br.ufmg.dcc.latin.features;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


import br.ufmg.dcc.latin.searcher.es.models.WeightingModel;

public class ConfigService {
	
	public void config(String configFilePath){
		try (BufferedReader br = new BufferedReader(new FileReader(configFilePath))) {
			String line;
			while ((line = br.readLine()) != null) {
				initConfig(line);
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void initConfig(String line){
		String[] splitLine = line.split(" ",2);
		String config = splitLine[0];
		String param = splitLine[1];
		switch (config) {
		case "QUERY_INDEPENDENT_FILENAME":
			Config.QUERY_INDEPENDENT_FILENAME = param;
			break;
			
		case "INITIAL_RANKING_MODEL":
			Class<?> act;
			try {
				act = Class.forName("br.ufmg.dcc.latin.searcher.es.models." + param);
	
				Config.INITIAL_RANKING_MODEL = (WeightingModel) act.newInstance();
			} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			break;
		case "ES_INDEX_NAME":
			Config.ES_INDEX_NAME = param;
			break;
		case "ES_INDEX_TYPE":
			Config.ES_INDEX_TYPE = param;
			break;
			
		case "OUTPUT_FILENAME":
			Config.OUTPUT_FILENAME = param;
			break;
		default:
			break;
		}
	}
    
}
