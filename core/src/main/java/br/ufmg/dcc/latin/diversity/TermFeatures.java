package br.ufmg.dcc.latin.diversity;

import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;
import br.ufmg.dcc.latin.retrieval.ReScorerController;
import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.set.hash.TIntHashSet;

public class TermFeatures implements Comparable<TermFeatures>{
	
	public int termId;
	public double[] features;
	public double weight;
	
	public TIntHashSet passageIds;
	
	public TermFeatures(int termId, int passageId, int relevance){
		this.termId = termId;
		this.features = new double[9];
		passageIds = new TIntHashSet();
		this.features[0] = collectionIdf(); // 2
		this.features[1] = 1f;  // 3
		this.features[2] = collectionIdf()*relevance;  // 4
		this.features[3] = relevance; // 5
		this.features[4] = googleNgram(); // 6
		this.features[5] = wikipediaTitles(); // 7
		this.features[6] = queryLog(); // 8
		this.features[7] = 0; // 9 // this term is in query
		this.features[8] = dbPediaEntities(passageId); // 8
		//this.features[9] = msEntities(passageId); // 9
		if (RetrievalController.queryTermsSet.contains(termId)){
			this.features[7] = 1; 
		}
		passageIds.add(passageId);
		
		

	//	this.features[6] = 0; // 8
	//	this.features[7] = 0; // 9
	}
	
	public TermFeatures(int termId){
		this.termId = termId;
		this.features = new double[9];
		this.features[0] = collectionIdf(); // 2
		this.features[1] = 1f;  // 3
		this.features[2] = collectionIdf()*4; // 4
		this.features[3] = 4; // 4
		this.features[4] = googleNgram(); // 5
		this.features[5] = wikipediaTitles(); // 6
		this.features[6] = queryLog(); // 7
		this.features[7] = 0; // 8
		if (RetrievalController.queryTermsSet.contains(termId)){
			this.features[7] = 1; 
		}
		
		//this.features[8] = dbPediaEntities(passageId); // 8
		//this.features[9] = msEntities(passageId); // 9
	//	this.features[6] = 0; // 8
	//	this.features[7] = 0; // 9
	}
	
	public void updateTerm(int passageId, int relevance){
		this.features[1] += 1;
		this.features[2] += collectionIdf()*relevance;
		this.features[3] += relevance;
		this.features[8] += dbPediaEntities(passageId);
		//this.features[9] += msEntities(passageId);
		passageIds.add(passageId);
	}
	
	public void updateTerm(){
		this.features[1] += 1;
		this.features[2] += collectionIdf()*4;
		this.features[3] += 4;
	//	this.features[7] += dbPediaEntities(passageId);
	//	this.features[7] += msEntities(passageId);
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

	@Override
	public int compareTo(TermFeatures o) {
		if (this.weight > o.weight) {
			return -1;
		}  else if (this.weight < o.weight){
			return 1;
		}
		return 0;
	}
}