package br.ufmg.dcc.latin.controller;

import java.util.Map;

public class DirectedIndexController {
	private Map<String,Float> idfs;
	private TermFrequencies[] termFrequencies;
	private float[] docNorms;
	public Map<String,Float> getIdfs() {
		return idfs;
	}
	public void setIdfs(Map<String,Float> idfs) {
		this.idfs = idfs;
	}
	public TermFrequencies[] getTermFrequencies() {
		return termFrequencies;
	}
	public void setTermFrequencies(TermFrequencies[] termFrequencies) {
		this.termFrequencies = termFrequencies;
	}
	public float[] getDocNorms() {
		return docNorms;
	}
	public void setDocNorms(float[] docNorms) {
		this.docNorms = docNorms;
	}

}
