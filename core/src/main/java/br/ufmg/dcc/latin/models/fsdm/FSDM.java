package br.ufmg.dcc.latin.models.fsdm;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;

public class FSDM {
	
	private ArrayList<String> fields = new ArrayList<String>();
	private HashMap<String, Double> uFieldsWeights = new HashMap<String, Double>();
	private HashMap<String, Double> bFieldsWeights = new HashMap<String, Double>();
	private HashMap<String, Double> wbFieldsWeights = new HashMap<String, Double>();
	private double uAlphaWeight = 0.8;
	private double bAlphaWeight = 0.1;
	private double wbAlphaWeight = 0.1;
	
	public FSDM(){
		fields.add("text");
		//fields.add("content");
		//fields.add("categories");
		//fields.add("similar_entities");
		//fields.add("related_entities");
		//fields.add("url");
		//fields.add("classes");
		//fields.add("all");
		
		uFieldsWeights.put("text", 1.0d);
		//uFieldsWeights.put("content", 1.0d);
		//uFieldsWeights.put("categories", 1.0d);
		//uFieldsWeights.put("similar_entities", 1.0d);
		//uFieldsWeights.put("related_entities", 1.0d);
		//uFieldsWeights.put("url", 1.0d);
		//uFieldsWeights.put("classes", 1.0d);
		//uFieldsWeights.put("all", 1.0d);
		
		bFieldsWeights.put("text", 1.0d);
		//bFieldsWeights.put("content", 1.0d);
		//bFieldsWeights.put("categories", 1.0d);
		//bFieldsWeights.put("similar_entities", 1.0d);
		//bFieldsWeights.put("related_entities", 1.0d);
		//bFieldsWeights.put("url", 1.0d);
		//bFieldsWeights.put("classes", 1.0d);
		//bFieldsWeights.put("all", 1.0d);
		
		wbFieldsWeights.put("text", 1.0d);
		//wbFieldsWeights.put("content", 1.0d);
		//wbFieldsWeights.put("categories", 1.0d);
		//wbFieldsWeights.put("similar_entities", 1.0d);
		//wbFieldsWeights.put("related_entities", 1.0d);
		//wbFieldsWeights.put("url", 1.0d);
		//wbFieldsWeights.put("classes", 1.0d);
		//wbFieldsWeights.put("all", 1.0d);
	}
	
	public void setUnigramFieldWeigth(String fieldName, double weight){
		uFieldsWeights.put(fieldName, weight);
	}
	
	public void setBigramFieldWeigth(String fieldName, double weight){
		bFieldsWeights.put(fieldName, weight);
	}
	
	public void setWbigramFieldWeigth(String fieldName, double weight){
		wbFieldsWeights.put(fieldName, weight);
	}
	
	public void setFieldWeight(String model, String fieldName, double weight){
		if(model.equals("unigrams"))
			setUnigramFieldWeigth(fieldName, weight);
		else if(model.equals("bigrams"))
			setBigramFieldWeigth(fieldName, weight);
		else
			setWbigramFieldWeigth(fieldName, weight);
	}
	
	public void setAllUnigramFieldsWeights(double[] weights){
		uFieldsWeights.put("name", weights[0]);
		uFieldsWeights.put("content", weights[1]);
		uFieldsWeights.put("categories", weights[2]);
		uFieldsWeights.put("similar_entities", weights[3]);
		uFieldsWeights.put("related_entities", weights[4]);
		//uFieldsWeights.put("url", weights[5]);
		//uFieldsWeights.put("classes", weights[6]);
		//uFieldsWeights.put("all", weights[7]);
	}
	
	public void setAllBigramFieldsWeights(double[] weights){
		bFieldsWeights.put("name", weights[0]);
		bFieldsWeights.put("content", weights[1]);
		bFieldsWeights.put("categories", weights[2]);
		bFieldsWeights.put("similar_entities", weights[3]);
		bFieldsWeights.put("related_entities", weights[4]);
		//bFieldsWeights.put("url", weights[5]);
		//bFieldsWeights.put("classes", weights[6]);
		//bFieldsWeights.put("all", weights[7]);
	}
	
	public void setAllWbigramFieldsWeights(double[] weights){
		wbFieldsWeights.put("name", weights[0]);
		wbFieldsWeights.put("content", weights[1]);
		wbFieldsWeights.put("categories", weights[2]);
		wbFieldsWeights.put("similar_entities", weights[3]);
		wbFieldsWeights.put("related_entities", weights[4]);
		//wbFieldsWeights.put("url", weights[5]);
		//wbFieldsWeights.put("classes", weights[6]);
		//wbFieldsWeights.put("all", weights[7]);
	}
	
	public void setUnigramAlphaWeight(double weight){
		uAlphaWeight = weight;
	}
	
	public void setBigramAlphaWeight(double weight){
		bAlphaWeight = weight;
	}
	
	public void setWbigramAlphaWeight(double weight){
		wbAlphaWeight = weight;
	}
	
	public double unigramScore(JsonNode uTerms, Document doc, JsonNode fieldsStats, JsonNode uCollFreq){
		double unigramScore = 0;
		JsonNode termsNode = uTerms.path(uTerms.fieldNames().next());
		for(JsonNode termNode : termsNode){
			String term = termNode.asText();
			unigramScore += modelScore(term, doc.getFieldsLengths(), doc.getUnigramsFrequencies(), fieldsStats, uCollFreq, uFieldsWeights);
		}
		
		return unigramScore;
	}
	
	public double bigramScore(JsonNode bTerms, Document doc, JsonNode fieldsStats, JsonNode bCollFreq){
		double bigramScore = 0;
		JsonNode termsNode = bTerms.path(bTerms.fieldNames().next());
		for(JsonNode termNode : termsNode){
			String term = termNode.asText();
			bigramScore += modelScore(term, doc.getFieldsLengths(), doc.getBigramsFrequencies(), fieldsStats, bCollFreq, bFieldsWeights);
		}
		
		return bigramScore;
	}
	
	public double windowBigramScore(JsonNode bTerms, Document doc, JsonNode fieldsStats){
		double wbigramScore = 0;
		JsonNode termsNode = bTerms.path(bTerms.fieldNames().next());
		for(JsonNode termNode : termsNode){
			String term = termNode.asText();
			wbigramScore += modelScore(term, doc.getFieldsLengths(), doc.getWbigramsFrequencies(), fieldsStats, null,wbFieldsWeights);
		}
		
		return wbigramScore;
	}
	
	public double modelScore(String term, HashMap<String, Integer> docFieldsLength, HashMap<String, HashMap<String, Integer>> docTfFields, JsonNode fieldsStats, JsonNode collFreq, HashMap<String, Double> fieldsWeights){
		double score = 0;
		
		for(String field : fields){
			double mu = fieldsStats.path(field).path("avg_length").asDouble();
			double C = fieldsStats.path(field).path("sum_ttf").asDouble();
			double D = docFieldsLength.get(field);
			double w = fieldsWeights.get(field);
			
			int tfD = docTfFields.get(field).containsKey(term) ? docTfFields.get(field).get(term) : 0; 	
			int tfC = (collFreq != null) ? collFreq.path(field).path(term).asInt() : /*wbigrams*/ (fieldsStats.path(field).path("doc_count").asInt() / 100) * 2;
			
			
			double numerator = tfD + mu * (tfC / C);
			double denominator = D + mu;
			
			score += w * (numerator / denominator);
		}
		
		if(score != 0)
			score = Math.log10(score);
		
		return score;
	}
	
	public double finalScore(JsonNode uTerms, JsonNode bTerms, Document doc, JsonNode fieldsStats, JsonNode uCollFreq, JsonNode bCollFreq){
		return uAlphaWeight * unigramScore(uTerms, doc, fieldsStats, uCollFreq) + 
			   bAlphaWeight * bigramScore(bTerms, doc, fieldsStats, bCollFreq)  +
			   wbAlphaWeight * windowBigramScore(bTerms, doc, fieldsStats);
	}
	
}
