package br.ufmg.dcc.latin.controller;

import java.util.HashMap;
import java.util.Map;

public class TermFrequencies {
	private Map<String,Float> freqs;
	
	public TermFrequencies(){
		freqs = new HashMap<String,Float>();
	}

	public Map<String,Float> getFreqs() {
		return freqs;
	}

	public void setFreqs(Map<String,Float> freqs) {
		this.freqs = freqs;
	}
}
