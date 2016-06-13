package br.ufmg.dcc.latin.searcher.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.lucene.search.Explanation;
import org.elasticsearch.common.settings.Settings;

import br.ufmg.dcc.latin.searcher.models.WeightingModel;
import br.ufmg.dcc.latin.searcher.utils.PropertyDetails;
import br.ufmg.dcc.latin.searcher.utils.TermDetails;

public class BM25 implements WeightingModel {	
	
	private Double b;
	private Double k1;
	
	public BM25(Double b, Double k1) {
		this.b = b;
		this.k1 = k1;
	}
	
	public BM25() {
		this.b = 2.0;
		this.k1 = 1.0;
	}

	@Override
	public Settings getSettings() {
        Settings settings = Settings.settingsBuilder()
            .put("index.similarity.default.type","BM25")
            .put("index.similarity.default.b",this.b)
            .put("index.similarity.default.k1",this.k1)
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
		
		propertyDetails.getPropertyDetails().put("IDF", new TermDetails());
		propertyDetails.getPropertyDetails().put("TF", new TermDetails());
		propertyDetails.getPropertyDetails().put("DL", new TermDetails());
		propertyDetails.getPropertyDetails().put("ADL", new TermDetails());

		
		lists.put("IDF", new ArrayList<Double>());
		lists.put("TF", new ArrayList<Double>());
		lists.put("DL", new ArrayList<Double>());
		lists.put("ADL", new ArrayList<Double>());
		
		computeDetailsBM25(explanation,lists, terms);
		for (int i = 0; i < terms.size(); i++) {
			propertyDetails.getPropertyDetails().get("IDF").getTermDetails().put(terms.get(i), lists.get("IDF").get(i));
			propertyDetails.getPropertyDetails().get("TF").getTermDetails().put(terms.get(i), lists.get("TF").get(i));
			propertyDetails.getPropertyDetails().get("DL").getTermDetails().put(terms.get(i), lists.get("DL").get(i));
			propertyDetails.getPropertyDetails().get("ADL").getTermDetails().put(terms.get(i), lists.get("ADL").get(i));

		}
		
		return propertyDetails;
	}
	
	private void computeDetailsBM25(Explanation explanation, HashMap<String, List<Double>> details, List<String> terms){
		
		if (explanation.getDescription().startsWith("weight")){
			String[] splitTerm = explanation.getDescription().split(":",2);
			String[] term = splitTerm[1].split(" ");
			terms.add(term[0]);
		}
		
		if (explanation.getDescription().contains("idf")){
			details.get("IDF").add((double) explanation.getValue());
		} else if (explanation.getDescription().contains("termFreq")) {
			details.get("TF").add((double) explanation.getValue());
		} else if (explanation.getDescription().contains("fieldLength")) {
			details.get("DL").add((double) explanation.getValue());
		} else if (explanation.getDescription().contains("avgFieldLength")) {
			details.get("ADL").add((double) explanation.getValue());
		} 
		
		for (Explanation ex : explanation.getDetails()) {
			computeDetailsBM25(ex,details,terms);
		}
		
	}
}
