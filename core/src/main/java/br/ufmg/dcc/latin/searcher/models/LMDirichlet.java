package br.ufmg.dcc.latin.searcher.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.utils.Details;
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
	public Details getDetails(Explanation explanation) {
		Details propertyDetails = new Details();
		
		
		HashMap<String, List<Double>> lists = new HashMap<String, List<Double>>();
		List<String> terms = new ArrayList<String>();
		
		propertyDetails.getTerms().put("Term_weight", new TermDetails());
		propertyDetails.getTerms().put("Document_norm", new TermDetails());
		propertyDetails.getTerms().put("Collection_probability", new TermDetails());
		
		
		
		lists.put("Term weight", new ArrayList<Double>());
		lists.put("Document norm", new ArrayList<Double>());
		lists.put("Collection probability", new ArrayList<Double>());
		
		
		computeDetailsLMDirichlet(explanation,lists, terms);
		
		for (int i = 0; i < terms.size(); i++) {
			propertyDetails.getTerms().get("Term_weight").getTermDetails().put(terms.get(i), lists.get("Term_weight").get(i));
			propertyDetails.getTerms().get("Document_norm").getTermDetails().put(terms.get(i), lists.get("Document_norm").get(i));
			propertyDetails.getTerms().get("Collection_probability").getTermDetails().put(terms.get(i), lists.get("Collection_probability").get(i));
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
			details.get("Term_weight").add((double) explanation.getValue());
		} else if (explanation.getDescription().contains("document norm")) {
			details.get("Document_norm").add((double) explanation.getValue());
		} else if (explanation.getDescription().contains("collection probability")) {
			details.get("Collection_probability").add((double) explanation.getValue());
		} 
		
		for (Explanation ex : explanation.getDetails()) {
			computeDetailsLMDirichlet(ex,details,terms);
		}
		
	}

}
