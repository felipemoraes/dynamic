package br.ufmg.dcc.latin.external;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import br.ufmg.dcc.latin.aspect.external.TermWeight;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class EntityLinkingCollection {
	
	Map<String, List<TermWeight> > entities;
	
	public EntityLinkingCollection(String filename) {
		
		entities = new HashMap<String,List<TermWeight>>();
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = br.readLine();
		
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split("<sep3>",2);
				String passage = splitLine[1];
				String[] keywords = splitLine[0].split("<sep2>");
				List<TermWeight> termWeights = new ArrayList<TermWeight>();
				for (int i = 0; i < keywords.length; i++) {
					if (keywords[i].length() > 0) {
						String[] wordsScore  = keywords[i].split("<sep1>");
						float score = Float.parseFloat(wordsScore[1]);

						
						List<String> words = tokenizeString(RetrievalController.getAnalyzer(), wordsScore[0]);
						for (String word : words) {
							termWeights.add(new TermWeight(word,score));
						}
						
					}
				}
				entities.put(passage, termWeights);
		    			
			}
			
	
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	protected List<String> tokenizeString(Analyzer analyzer, String str) {
		List<String> result = new ArrayList<String>();
		try {
		      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
		      stream.reset();
	
		      while (stream.incrementToken()) {
		    	  result.add(stream.getAttribute(CharTermAttribute.class).toString());
		      } 
		      
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result;
		
	}
	
	public List<TermWeight> getTermWeights(String passage){
		List<TermWeight> termWeights = new ArrayList<TermWeight>();
		if (entities.containsKey(passage)){
			return entities.get(passage);
		}
		return termWeights;
	}

}
