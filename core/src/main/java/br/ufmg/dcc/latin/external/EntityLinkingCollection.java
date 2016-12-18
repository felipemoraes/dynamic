package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class EntityLinkingCollection {
	
	TIntDoubleHashMap[] invertedEntityIndex;
	
	public EntityLinkingCollection(String filename) {
		
		int n = RetrievalController.vocab[0].size();
		invertedEntityIndex = new TIntDoubleHashMap[n];
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
		
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(",",2);
				String keyword = splitLine[0];
				int termId = RetrievalController.vocab[0].getId(keyword);
				String[] occurrences = splitLine[1].split(",");
				TIntDoubleHashMap map = new TIntDoubleHashMap();
				for (String occur : occurrences) {
					String[] splitOccur = occur.split(":");
					if (splitOccur.length < 2) {
						continue;
					}
					map.put(Integer.parseInt(splitOccur[0]), Double.parseDouble(splitOccur[1]));
				}
				
		    	if (termId == -1) {
		    		continue;
		    	}
				invertedEntityIndex[termId] = map;
		    			
			}
			
	
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public double getKeyWordScore(int termId, int passageId){
		if (invertedEntityIndex[termId] == null) {
			return 0d;
		}
		if (invertedEntityIndex[termId].containsKey(passageId)){
			return invertedEntityIndex[termId].get(passageId);
		}
		return 0d;
	}

}
