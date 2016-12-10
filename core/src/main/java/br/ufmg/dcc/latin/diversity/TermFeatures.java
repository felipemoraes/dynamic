package br.ufmg.dcc.latin.diversity;

import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class TermFeatures {
	
	public String term;
	public double[] features;
	public double weight;
	
	public TermFeatures(String term, int passageId, int relevance){
		this.term = term;
		this.features = new double[8];
		this.features[0] = collectionIdf();
		this.features[1] = 1f;
		this.features[2] = relevance;
		this.features[3] = googleNgram();
		this.features[4] = wikipediaTitles();
		this.features[5] = queryLog();
		this.features[6] = dbPediaEntities(passageId);
		this.features[7] = msEntities(passageId);

	}
	
	public void updateTerm(int passageId, int relevance){
		this.features[1] += 1;
		this.features[2] += relevance;
		this.features[6] += dbPediaEntities(passageId);
		this.features[7] += msEntities(passageId);
	}
	
	private double msEntities( int passageId) {
		return ExternalKnowledgeCache.msEntityLinkingCollection.getKeyWordScore(term, passageId);
	}



	private double dbPediaEntities(int passageId) {
		return ExternalKnowledgeCache.dbpediaEntityLinkingCollection.getKeyWordScore(term, passageId);
	}



	private double queryLog() {
		float numDocs = ExternalKnowledgeCache.queryLog.getNumDocs();
		float docFreq = ExternalKnowledgeCache.queryLog.getDocFreq(term);
		return (float) Math.log(numDocs/(docFreq+1));
	}



	private double wikipediaTitles() {
		float numDocs = ExternalKnowledgeCache.wikipedia.getNumDocs();
		float docFreq = ExternalKnowledgeCache.wikipedia.getDocFreq(term);
		return (float) Math.log(numDocs/(docFreq+1));
	}



	private double googleNgram() {
		float sumTotalTermFreq = ExternalKnowledgeCache.ngram.getSumTotalTermFreq();
		float ttf = ExternalKnowledgeCache.ngram.getTotalTermFreq(term);
		return (float) Math.log(sumTotalTermFreq/(ttf+1));
	}



	private double collectionIdf() {
		double[] weights = RetrievalController.getFiedlWeights();
		double idf = weights[0]*RetrievalController.getIdf("title", term)
					+ weights[1]*RetrievalController.getIdf( "content", term);
		return idf ;
	}
}