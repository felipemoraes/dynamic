package br.ufmg.dcc.latin.diversity;

import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;
import br.ufmg.dcc.latin.retrieval.ReScorerController;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class TermFeatures {
	
	public int termId;
	public double[] features;
	public double weight;
	
	public TermFeatures(int termId, int passageId, int relevance){
		this.termId = termId;
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
		return ExternalKnowledgeCache.msEntityLinkingCollection.getKeyWordScore(termId, passageId);
	}



	private double dbPediaEntities(int passageId) {
		return ExternalKnowledgeCache.dbpediaEntityLinkingCollection.getKeyWordScore(termId, passageId);
	}



	private double queryLog() {
		float numDocs = ExternalKnowledgeCache.queryLog.getNumDocs();
		float docFreq = ExternalKnowledgeCache.queryLog.getDocFreq(termId);
		return (float) Math.log(numDocs/(docFreq+1));
	}



	private double wikipediaTitles() {
		float numDocs = ExternalKnowledgeCache.wikipedia.getNumDocs();
		float docFreq = ExternalKnowledgeCache.wikipedia.getDocFreq(termId);
		return (float) Math.log(numDocs/(docFreq+1));
	}



	private double googleNgram() {
		float sumTotalTermFreq = ExternalKnowledgeCache.ngram.getSumTotalTermFreq();
		float ttf = ExternalKnowledgeCache.ngram.getTotalTermFreq(termId);
		return (float) Math.log(sumTotalTermFreq/(ttf+1));
	}



	private double collectionIdf() {
		double[] weights = RetrievalController.getFiedlWeights();
		double idf = weights[0]*ReScorerController.getIdf("title", termId)
					+ weights[1]*ReScorerController.getIdf( "content", termId);
		return idf ;
	}
}