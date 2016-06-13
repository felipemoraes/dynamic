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
	
	public Details(){
		docsDetails = new HashMap<String, PropertyDetails>();
	}

	public HashMap<String, PropertyDetails> getDocsDetails() {
		return docsDetails;
	}

	public void setDocsDetails(HashMap<String, PropertyDetails> docsDetails) {
		this.docsDetails = docsDetails;
	}


}
