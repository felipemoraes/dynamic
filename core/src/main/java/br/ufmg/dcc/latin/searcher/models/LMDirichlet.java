package br.ufmg.dcc.latin.searcher.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.utils.PropertyDetails;
import br.ufmg.dcc.latin.searcher.utils.TermDetails;

public class LMDirichlet implements WeightingModel {

	private Double mu;
	
	public LMDirichlet(){
		this.mu = 2000.0;
	}
	
	public LMDirichlet(Double mu){
		this.mu = mu;
	}
	
	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","LMDirichlet")
            .put("index.similarity.default.mu",this.mu)
            .build();
		return settings;
	}

	/* (non-Javadoc)
	 * @see br.ufmg.dcc.latin.searcher.models.WeightingModel#computeDetails(org.apache.lucene.search.Explanation)
	 */
	@Override
	public PropertyDetails getDetails(Explanation explanation) {
		PropertyDetails propertyDetails = new PropertyDetails();
		
		
		HashMap<String, List<Double>> lists = new HashMap<String, List<Double>>();
		List<String> terms = new ArrayList<String>();
		
		propertyDetails.getPropertyDetails().put("Term weight", new TermDetails());
		propertyDetails.getPropertyDetails().put("Document norm", new TermDetails());
		propertyDetails.getPropertyDetails().put("Collection probability", new TermDetails());
		
		
		
		lists.put("Term weight", new ArrayList<Double>());
		lists.put("Document norm", new ArrayList<Double>());
		lists.put("Collection probability", new ArrayList<Double>());
		
		
		computeDetailsLMDirichlet(explanation,lists, terms);
		
		for (int i = 0; i < terms.size(); i++) {
			propertyDetails.getPropertyDetails().get("Term weight").getTermDetails().put(terms.get(i), lists.get("Term weight").get(i));
			propertyDetails.getPropertyDetails().get("Document norm").getTermDetails().put(terms.get(i), lists.get("Document norm").get(i));
			propertyDetails.getPropertyDetails().get("Collection probability").getTermDetails().put(terms.get(i), lists.get("Collection probability").get(i));
		}
		
		return propertyDetails;
	}
	
	private void computeDetailsLMDirichlet(Explanation explanation, HashMap<String, List<Double>> details, List<String> terms){
		
		if (explanation.getDescription().startsWith("weight")){
			String[] splitTerm = explanation.getDescription().split(":",2);
			String[] term = splitTerm[1].split(" ");
			terms.add(term[0]);
		}
		
		if (explanation.getDescription().contains("term weight")){
			details.get("Term weight").add((double) explanation.getValue());
		} else if (explanation.getDescription().contains("document norm")) {
			details.get("Document norm").add((double) explanation.getValue());
		} else if (explanation.getDescription().contains("collection probability")) {
			details.get("Collection probability").add((double) explanation.getValue());
		} 
		
		for (Explanation ex : explanation.getDetails()) {
			computeDetailsLMDirichlet(ex,details,terms);
		}
		
	}

}
