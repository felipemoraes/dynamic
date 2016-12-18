package br.ufmg.dcc.latin.reranking;

import java.util.List;

import br.ufmg.dcc.latin.cache.ExternalKnowledgeCache;
import br.ufmg.dcc.latin.diversity.FeaturedAspect;
import br.ufmg.dcc.latin.diversity.TermFeatures;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.index.DocVec;
import br.ufmg.dcc.latin.retrieval.ReScorerController;
import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class QE extends InteractiveReranker {
	
	public QE(){
	}

	double lambda;
	
	double[] rereranking;
	
	double[] aspectWeights;
	
	
	FeaturedAspect featuredAspect;
	
	TIntArrayList queryTerms;
	
	@Override
	public String debug() {
		return null;
	}
	

	@Override
	protected double score(int docid) {
		return (1-lambda)*relevance[docid] + lambda*rereranking[docid];
	}

	@Override
	protected void update(int docid) {
	}
	
	
	public TIntDoubleHashMap getAspectQuery(double[] weights) {
		
		TermFeatures[] topTerms = featuredAspect.getTopTerms(weights);
		TIntDoubleHashMap complexQuery = new TIntDoubleHashMap();
		System.out.print("Query: ");
		for (int i = 0; i < topTerms.length; i++) {
			double weight = topTerms[i].weight;
			System.out.print(RetrievalController.vocab[0].getTerm(topTerms[i].termId) + " " + weight + " ");
			complexQuery.put(topTerms[i].termId, weight);
		}
		System.out.println();
		return complexQuery;
	}
	@Override
	public void start(double[] params){
		super.start(params);
		RetrievalController.initQueryTerms(query);
		ExternalKnowledgeCache.init();
		relevance = normalize(relevance);
		lambda = params[1];
		
		featuredAspect = new FeaturedAspect();
		
		aspectWeights = new double[8];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		rereranking = new double[relevance.length];
	}

	@Override
	public void setParams(double[] params){
		super.start(params);
		lambda = params[1];
		aspectWeights = new double[8];
		for (int i = 2; i < params.length; i++) {
			aspectWeights[i-2] = params[i];
		}
		
		for (int i = 0; i < queryTerms.size(); i++) {
			featuredAspect.putTerm(queryTerms.get(i));
		}
		
		
	}
	@Override
	public void start(String query, String index){
		super.start(query, index);
		List<String> terms = RetrievalController.tokenizeText(query);
		queryTerms = new TIntArrayList();
		for (String term : terms) {
			int termId = RetrievalController.vocab[0].getId(term);
			queryTerms.add(termId);
		}
	}
	
	
	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		for (int i = 0; i < feedback.length; i++) {
			if (!feedback[i].onTopic) {
				continue;
			}
			for (int j = 0; j < feedback[i].passages.length; j++) {
				int passageId = feedback[i].passages[j].passageId;
				int relevance = feedback[i].passages[j].relevance;
				DocVec passageDocVec = RetrievalController.getPassageTerms(passageId);
				int[] terms = passageDocVec.vec.keys();
				for (int k = 0; k < terms.length; k++) {
					featuredAspect.putTerm(terms[k], passageId, relevance);
				}
			}
		}
		
		TIntDoubleHashMap complexQuery = getAspectQuery(aspectWeights);
		rereranking = normalize(ReScorerController.rescore(complexQuery));
		
	}

}
