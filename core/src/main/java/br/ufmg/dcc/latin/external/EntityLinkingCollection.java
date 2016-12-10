package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.util.BytesRef;

public class EntityLinkingCollection {
	
	Map<String, Map<Integer,Double> > invertedEntityIndex;
	
	public EntityLinkingCollection(String filename) {
		
		invertedEntityIndex = new HashMap<String, Map<Integer,Double> >();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
		
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(",",2);
				String keyword = splitLine[0];
				String[] occurrences = splitLine[1].split(",");
				Map<Integer,Double> map = new HashMap<Integer,Double>();
				for (String occur : occurrences) {
					String[] splitOccur = occur.split(":");
					if (splitOccur.length < 2) {
						continue;
					}
					map.put(Integer.parseInt(splitOccur[0]), Double.parseDouble(splitOccur[1]));
				}
				invertedEntityIndex.put(keyword, map);
		    			
			}
			
	
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public double getKeyWordScore(String term, int passageId){
		if (invertedEntityIndex.containsKey(term)){
			return invertedEntityIndex.get(term).getOrDefault(passageId, 0d);
		}
		return 0d;
	}

}
