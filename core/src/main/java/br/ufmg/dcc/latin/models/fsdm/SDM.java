package br.ufmg.dcc.latin.models.fsdm;

import java.util.HashMap;

import com.fasterxml.jackson.databind.JsonNode;

public class SDM {
	private double uModelWeight = 0.8;
	private double bModelWeight = 0.1;
	private double wbModelWeight = 0.1;
	
	public SDM(){
		
	}
	
	public void setUnigramModelWeights(double weight){
		uModelWeight = weight;
	}
	
	public void setBigramModelWeights(double weight){
		bModelWeight = weight;
	}
	
	public void setWbigramModelWeights(double weight){
		wbModelWeight = weight;
	}
	
	public double unigramScore2(JsonNode uTerms, Document doc, JsonNode fieldsStats, JsonNode uCollFreq){
		double unigramScore = 0;
		JsonNode termsNode = uTerms.path(uTerms.fieldNames().next());
		for(JsonNode termNode : termsNode){
			String term = termNode.asText();
			//System.out.print(doc.getUrl()+" -> ");
			unigramScore += modelScore2(term, doc.getFieldsLengths(), doc.getUnigramsFrequencies(), fieldsStats, uCollFreq);
		}
		
		return unigramScore;
	}
	
	public double bigramScore2(JsonNode bTerms, Document doc, JsonNode fieldsStats, JsonNode bCollFreq){
		double bigramScore = 0;
		JsonNode termsNode = bTerms.path(bTerms.fieldNames().next());
		for(JsonNode termNode : termsNode){
			String term = termNode.asText();
			bigramScore += modelScore2(term, doc.getFieldsLengths(), doc.getBigramsFrequencies(), fieldsStats, bCollFreq);
		}
		
		return bigramScore;
	}
	
	public double windowBigramScore2(JsonNode bTerms, Document doc, JsonNode fieldsStats){
		double wBigramScore = 0;
		JsonNode termsNode = bTerms.path(bTerms.fieldNames().next());
		for(JsonNode termNode : termsNode){
			String term = termNode.asText();
			wBigramScore += modelScore2(term, doc.getFieldsLengths(), doc.getWbigramsFrequencies(), fieldsStats, null);
		}
		
		return wBigramScore;
	}
	
	public double modelScore2(String term, HashMap<String, Integer> docFieldsLength, HashMap<String, HashMap<String, Integer>> docTfFields, JsonNode fieldsStats, JsonNode collFreq){
		
		double mu = fieldsStats.path("all").path("avg_length").asDouble();
		double C = fieldsStats.path("all").path("sum_ttf").asDouble();
		double D = docFieldsLength.get("all");
		
		double tfD = docTfFields.get("all").containsKey(term) ? docTfFields.get("all").get(term) : 0; 	
		
		double tfC = (collFreq != null) ? collFreq.path("all").path(term).asInt() : /*wbigrams:*/ (fieldsStats.path("all").path("doc_count").asInt() / 100) * 2; 

			
		double numerator = tfD + mu * (tfC / C);
		double denominator = D + mu;
			
		double score = (numerator / denominator);
		
		//System.out.println(tfD+" + "+mu+" * ("+tfC+" / "+C+")  /  "+D+" + "+mu+" = "+score);
			
		if(score != 0)
			score = Math.log10(score);
			
		return score;
	}
	
	public double finalScore2(JsonNode uTerms, JsonNode bTerms, Document doc, JsonNode fieldsStats, JsonNode uCollFreq, JsonNode bCollFreq){
		return uModelWeight * unigramScore2(uTerms, doc, fieldsStats, uCollFreq) + 
			   bModelWeight * bigramScore2(bTerms, doc, fieldsStats, bCollFreq)  +
			   wbModelWeight * windowBigramScore2(bTerms, doc, fieldsStats);
	}
}
