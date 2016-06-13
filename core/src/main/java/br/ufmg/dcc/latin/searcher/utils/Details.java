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
	// DocId, PropertyDetails
	private HashMap<String, PropertyDetails> docsDetails;

	public HashMap<String, PropertyDetails> getDetails() {
		return docsDetails;
	}

	public void setDetails(HashMap<String, PropertyDetails> details) {
		this.docsDetails = details;
	}

}
