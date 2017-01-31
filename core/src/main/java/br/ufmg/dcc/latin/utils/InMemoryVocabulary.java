package br.ufmg.dcc.latin.index;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.map.hash.TObjectIntHashMap;

public class InMemoryVocabulary {
	
	TObjectIntHashMap<String> vocab;
	String[] invVocab;
	
	public InMemoryVocabulary(Terms terms) {
		try {
			int n = 0;
			List<String> queryTerms = getAllQueryTerms();
			TermsEnum iterator = terms.iterator();
			BytesRef term =  iterator.next();
			n += queryTerms.size();
			while (term != null) {
				n++;
				term = iterator.next();
			}
			vocab = new TObjectIntHashMap<String>();
			vocab.ensureCapacity(n+1000);
			invVocab = new String[n+1000];
			iterator = terms.iterator();
			term =  iterator.next();
			while (term != null) {
				this.addTerm(term.utf8ToString());
				term = iterator.next();
			}
			for (String queryTerm : queryTerms) {
				this.addTerm(queryTerm);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private List<String> tokenizeText(String text){
		List<String> result = new ArrayList<String>();
		try {
		      TokenStream stream  = RetrievalController.getAnalyzer().tokenStream(null, new StringReader(text));
		      stream.reset();
	
		      while (stream.incrementToken()) {
		    	  BytesRef term = new BytesRef(stream.getAttribute(CharTermAttribute.class).toString());
		    	  result.add(term.utf8ToString());
		      } 
		      
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result;
	}
	
	private List<String> getAllQueryTerms(){
		List<String> allTerms = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader("../share/topics_domain.txt"));
			String line;
			while ((line = br.readLine()) != null) {
			    String[] splitLine = line.split(" ",3);
			    String query = splitLine[2];
			    allTerms.addAll(tokenizeText(query));
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return allTerms;
	}
	
	
	public int addTerm(String term){
		int termId = vocab.size();
		vocab.putIfAbsent(term, termId);
		invVocab[termId] = term;
		return termId;
	}
	
	public int getId(String term){
		if (vocab.containsKey(term)) {
			return vocab.get(term);
		} else {
			return -1;
		}
	}
	
	public String getTerm(int termId){
		return invVocab[termId];
	}
	
	public boolean contains(String term){
		return vocab.contains(term);
	}
	
	public int size(){
		return vocab.size();
	}
	
}
