/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Felipe Moraes
 *
 */
public class CollectionTerms {
	// field, DocTerms
	private Map<String, DocTerms> docTerms;
	
	
	public CollectionTerms(){
		docTerms = new HashMap<String, DocTerms>();
	}
	
	public class DocTerms {
		// docId, term
		private Map<String, TermsFrequency> terms;
		
		public DocTerms(){
			terms = new HashMap<String, TermsFrequency>();
		}
		
	}
	
	public class TermsFrequency {
		private Map<String,Long> frequecies;
		
		public TermsFrequency(){
			frequecies = new  HashMap<String,Long>();
		}
	}

	public Map<String,Long> getDocTerms(String field, String docId) {
		if (docTerms.get(field).terms.containsKey(docId)) {
			return docTerms.get(field).terms.get(docId).frequecies;
		}
		return new HashMap<String,Long>();
		
	}
	
	private void checkFieldAndDocExistAndThenCreate(String field, String docId){
		if (!docTerms.containsKey(field)) {
			docTerms.put(field, new DocTerms());
		}
		
		if (!docTerms.get(field).terms.containsKey(docId)) {
			docTerms.get(field).terms.put(docId, new TermsFrequency());
		}
	}

	public Boolean checkDocExist(String field, String docId){
		if (!docTerms.containsKey(field)) {
			return false;
		}
		
		if (!docTerms.get(field).terms.containsKey(docId)) {
			return false;
		}
		return true;
	}
	
	public void setDocTerms(String field, String docId, Map<String,Long> frequecies) {
		checkFieldAndDocExistAndThenCreate(field,docId);
		docTerms.get(field).terms.get(docId).frequecies = frequecies;
	}

}
