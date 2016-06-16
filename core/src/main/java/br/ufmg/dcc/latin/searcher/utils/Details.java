/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;

/**
 * @author Felipe Moraes
 *
 */
public class Details {
	private HashMap<String, TermDetails> terms;
	
	public Details(){
		this.setTerms(new HashMap<String, TermDetails>());
	}

	public HashMap<String, TermDetails> getTerms() {
		return terms;
	}

	public void setTerms(HashMap<String, TermDetails> terms) {
		this.terms = terms;
	}

}
