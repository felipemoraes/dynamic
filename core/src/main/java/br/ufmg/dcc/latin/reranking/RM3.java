package br.ufmg.dcc.latin.reranking;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.index.DocVec;
import br.ufmg.dcc.latin.retrieval.ReScorerController;
import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.hash.TIntHashSet;

public class RM3 extends InteractiveReranker {
	
	List<DocVec> termCounts;
	TIntArrayList relevances;
	TIntArrayList docLens;
	TIntHashSet terms;
	TDoubleArrayList[] queryLikelihoodForPassages;
	String query;
	String indexName;
	
	double[] originalRelevance;
	
	
	
	float mu;
	float muFB;
	double lambda;
	int numberOfTerms;

	@Override
	protected double score(int docid) {
		return relevance[docid];
	}

	@Override
	protected void update(int docid) {
	}
	
	@Override
	public void start(double[] params){
		super.start(params);
		mu = 2000f;
		muFB = 2000f;
		numberOfTerms = (int) params[1];
		lambda = params[2];
		termCounts = new ArrayList<DocVec>();
		relevances = new TIntArrayList();
		docLens = new TIntArrayList();
		terms = new TIntHashSet();
		
		for (int i = 0; i < relevance.length; i++) {
			relevance[i] = originalRelevance[i];
		}

		queryLikelihoodForPassages = new TDoubleArrayList[2];
		queryLikelihoodForPassages[0] = new TDoubleArrayList();
		queryLikelihoodForPassages[1] = new TDoubleArrayList();

	}
	
	@Override
	public void start(String query, String index){
		super.start(query,index);
		this.query = query;
		this.indexName = index;
		originalRelevance = new double[relevance.length];
		for (int i = 0; i < relevance.length; i++) {
			originalRelevance[i] = relevance[i];
			
		}
		RetrievalController.init(RetrievalCache.topicId);
	}
	
	@Override
	public void update(Feedback[] feedback) {
		super.update(feedback);
		List<WeightedTerm> weightedTerms = ExpandRM1(feedback, "content",true);
		weightedTerms = ExpandRM3(weightedTerms);
		TIntDoubleHashMap complexQuery = getComplexQuery(weightedTerms);
		double[] scores = normalize(ReScorerController.rescore(complexQuery));
		
		for (int i = 0; i < scores.length; i++) {
			relevance[i] = scores[i];
		}
	}
	
	private TIntDoubleHashMap getComplexQuery(List<WeightedTerm> weightTerms){
		TIntDoubleHashMap complexQuery = new TIntDoubleHashMap();
		for (int i = 0; i < weightTerms.size(); i++) {
			complexQuery.put(weightTerms.get(i).termId, weightTerms.get(i).score);
		}
		return complexQuery;
	}
	
	
	private TIntDoubleHashMap queryLikehood(String query){
		TIntDoubleHashMap probs = new TIntDoubleHashMap();
		TIntArrayList terms = tokenizeString(RetrievalController.getAnalyzer(), query);
		double size = terms.size();
		
		for (int i = 0; i < size; i++) {
			probs.putIfAbsent(terms.get(i), 0d);
			probs.put(terms.get(i), probs.get(i)+1);
			
		}
		for (int i = 0; i < size; i++) {
			probs.put(terms.get(i), probs.get(i)/size);
			
		}
		
		return probs;
	}
	
	private List<WeightedTerm> ExpandRM3(List<WeightedTerm> weightedTerms) {
		
		List<WeightedTerm> newWeightedTerms = new ArrayList<WeightedTerm>();
		TIntDoubleHashMap probs = queryLikehood(query);
		
		float normFactor = 0;
		for (int i = 0; i < weightedTerms.size(); i++) {
			if (probs.containsKey(weightedTerms.get(i).termId)){
				weightedTerms.get(i).score = (1-lambda)*probs.get(weightedTerms.get(i).termId) + lambda*weightedTerms.get(i).score;
				
			} else {
				weightedTerms.get(i).score = lambda*weightedTerms.get(i).score;
				
			}
			normFactor += weightedTerms.get(i).score;
		}
		for (int i = 0; i < newWeightedTerms.size(); i++) {
			weightedTerms.get(i).score /= normFactor;
		}
		Collections.sort(weightedTerms);
		
		for (int i = 0; i < Math.min(numberOfTerms,weightedTerms.size()); i++) {
			newWeightedTerms.add(weightedTerms.get(i));
		}
		return newWeightedTerms;
	}

	private List<WeightedTerm> ExpandRM1(Feedback[] feedback, String field, boolean addFeedback){
		// compute count for 
		TIntIntHashMap queryCounts = extractCounts(query);
		
		List<DocVec> newTermCounts = new ArrayList<DocVec>();
 		// for each document and passage in feedback, compute counts
		for (int i = 0; i < feedback.length; i++) {
			if (!addFeedback){
				break;
			}
			if (!feedback[i].onTopic){
				continue;
			}
			Passage[] passages = feedback[i].passages;
			for (int j = 0; j < passages.length; j++) {
				 DocVec counts = RetrievalController.getPassageTerms(passages[j].passageId);
				 newTermCounts.add(counts);
				 docLens.add(counts.docLen);
				 relevances.add(passages[j].relevance);
			}
		}
		
		termCounts.addAll(newTermCounts);
		
		// compute QLE for each passage
		for (DocVec docVecCounts : newTermCounts) {
			double qle = queryLikehood(queryCounts,docVecCounts,"content");
			queryLikelihoodForPassages[0].add(qle);
			qle = queryLikehood(queryCounts,docVecCounts,"title");
			queryLikelihoodForPassages[1].add(qle);
		}
		
		
		List<WeightedTerm> weightedTerms = new ArrayList<WeightedTerm>();
		int ix = field.equals("content") ? 0 : 1;
		double sumTotalWeights = 0;
		for (int termId: terms._set) {
			double weight = 0;
			for (int i = 0; i < termCounts.size(); ++i) {
				double docLen = docLens.get(i);
				weight += relevances.get(i)*queryLikehoodTerm(termId,termCounts.get(i).vec.get(termId), docLen,field)*queryLikelihoodForPassages[ix].get(i) ;
			}
			weightedTerms.add(new WeightedTerm(termId, weight));
			sumTotalWeights += weight;
		}
		
		for (int i = 0; i < weightedTerms.size(); i++) {
			if (sumTotalWeights == 0) {
				break;
			}
			weightedTerms.get(i).score /= sumTotalWeights;
		}
		
		return weightedTerms;
	}
	

	private double queryLikehoodTerm(int termId, double termCount, double docLen, String field){
		double score = 0;
		score = termCount + muFB*collectionProbability(termId,field);
		if (score == 0) {
			return 0;
		}
		score /= (docLen + muFB);
		return score;
	}
	
	private double queryLikehood(TIntIntHashMap queryCounts, DocVec passageDocVec, String field){
		double score = 1;
		double docLen = passageDocVec.docLen;
		int[] terms = queryCounts.keys();
		for (int i = 0; i < terms.length; i++) {
	
			double s = passageDocVec.vec.get(terms[i]) + mu*collectionProbability(terms[i], field);
			s /= (docLen + mu);
			score *= s;
		}
		return score;
	}
	
	private double collectionProbability(int termId, String field) {
		int ix = field.equals("content") ? 0 : 1;
		double ttf = RetrievalController.termStats[ix].docFreq[termId];
		double sttf = RetrievalController.directedIndex[ix].sumDocFreq;
		return ttf/sttf;
	}
	

	private TIntIntHashMap extractCounts(String text){
		
		TIntArrayList grams = tokenizeString(RetrievalController.getAnalyzer(), text);
		TIntIntHashMap counts = new TIntIntHashMap();
		for (int i = 0; i < grams.size(); ++i) {
			terms.add(grams.get(i));
			if (counts.contains(grams.get(i))) {
				int count = counts.get(grams.get(i));
				counts.put(grams.get(i), count+1);
			} else {
				counts.put(grams.get(i),1);
			}
		}
		return counts;
	}
	
	public TIntArrayList tokenizeString(Analyzer analyzer, String str) {
		TIntArrayList result = new TIntArrayList();
		try {
		      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
		      stream.reset();
	
		      while (stream.incrementToken()) {
		    	  BytesRef term = new BytesRef(stream.getAttribute(CharTermAttribute.class).toString());
		    	  int termId = RetrievalController.vocab[0].getId(term.utf8ToString());
		    	  result.add(termId);
		      } 
		      
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result;
		
	}
	
	@Override
	public String debug(String topicid, int iteration) {
		return null;
	}
	
	
	
	  public static class WeightedTerm implements Comparable<WeightedTerm>{
		    public int termId;
		    public double score;

		    public WeightedTerm(int termId, double score) {
		    	this.score = score;
		      
		    	this.termId = termId;
		    }


		    public int getTerm() {
		      return termId;
		    }
		    

		    public int compareTo(WeightedTerm other) {
		      int result = -Double.compare(this.score, other.score);
		      if (result != 0) {
		        return result;
		      }
		      result = (this.termId > other.termId) ? 1 : -1;
		      return result;
		    }

		    @Override
		    public String toString() {
		      return "<" + termId + "," + score + ">";
		    }
		  }

}
