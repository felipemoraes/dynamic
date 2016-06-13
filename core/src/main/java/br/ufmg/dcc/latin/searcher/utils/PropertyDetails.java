/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashMap;

/**
 * @author Felipe Moraes
 *
 */
public class PropertyDetails {
	private HashMap<String, TermDetails> propertyDetails;
	
	public PropertyDetails(){
		this.propertyDetails  = new HashMap<String, TermDetails>();
	}

	public HashMap<String, TermDetails> getPropertyDetails() {
		return propertyDetails;
	}

	public void setPropertyDetails(HashMap<String, TermDetails> propertyDetails) {
		this.propertyDetails = propertyDetails;
	}
}
