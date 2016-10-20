package br.ufmg.dcc.latin.models.fsdm;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class Document {
	
	private String _url;
	private HashMap<String, Integer> _fields_lengths = new HashMap<String, Integer>();
	
	private HashMap<String, HashMap<String, Integer>> _unigrams_frequencies = new HashMap<String, HashMap<String, Integer>>();
	
	private HashMap<String, HashMap<String, Integer>> _bigrams_frequencies = new HashMap<String, HashMap<String, Integer>>();
	
	private HashMap<String, HashMap<String, Integer>> _wbigrams_frequencies = new HashMap<String, HashMap<String, Integer>>();
	
	public  void setUrl(String url){
		this._url = url;
	}
	
	public String getUrl(){
		return _url;
	}
	
	public void setFields_lengths(HashMap<String, Integer> fieldsLengths){
		this._fields_lengths = fieldsLengths;
	}
	
	public HashMap<String, Integer> getFieldsLengths(){
		return _fields_lengths;
	}
	
	public void setUnigrams_frequencies(HashMap<String, HashMap<String, Integer>> unigrams){
		this._unigrams_frequencies = unigrams;
	}
	
	public HashMap<String, HashMap<String, Integer>> getUnigramsFrequencies(){
		return _unigrams_frequencies;
	}
	
	public void setBigrams_frequencies(HashMap<String, HashMap<String, Integer>> bigrams){
		this._bigrams_frequencies = bigrams;
	}
	
	public HashMap<String, HashMap<String, Integer>> getBigramsFrequencies(){
		return _bigrams_frequencies;
	}
	
	//@JsonDeserialize(using = FrequenciesDeserializer.class)
	public void setWbigrams_frequencies(HashMap<String, HashMap<String, Integer>> wbigrams){
		this._wbigrams_frequencies = wbigrams;
	}
	
	public HashMap<String, HashMap<String, Integer>> getWbigramsFrequencies(){
		return _wbigrams_frequencies;
	}
}
