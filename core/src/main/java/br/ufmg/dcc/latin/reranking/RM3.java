package br.ufmg.dcc.latin.reranking;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.RetrievalController;

public class RM3 extends InteractiveReranker {
	
	List<Map<String,Double>> termCounts;
	List<Double> relevances;
	List<Double> docLens;
	Set<String> terms;
	Map<String, List<Double>> queryLikelihoodForPassages;
	String query;
	String indexName;
	
	double[] originalRelevance;
	
	
	
	float mu;
	float muFB;
	float lambda;
	int numberOfTerms;

	@Override
	protected double score(int docid) {
		return relevance[docid];
	}

	@Override
	protected void update(int docid) {
	}
	
	@Override
	public void start(float[] params){
		super.start(params);
		mu = 2000f;
		muFB = 2000f;
		numberOfTerms = (int) params[1];
		lambda = params[2];
		termCounts = new ArrayList<Map<String,Double>>();
		relevances = new ArrayList<Double>();
		docLens = new ArrayList<Double>();
		terms = new HashSet<String>();
		
		for (int i = 0; i < relevance.length; i++) {
			relevance[i] = originalRelevance[i];
		}

		queryLikelihoodForPassages = new HashMap<String, List<Double>>();
		queryLikelihoodForPassages.put("content", new ArrayList<Double>());
		queryLikelihoodForPassages.put("title", new ArrayList<Double>());
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
		RetrievalController.loadDocFreqs(indexName);
	}
	
	@Override
	public void update(Feedback[] feedback) {
		
		List<WeightedTerm> weightedTerms = ExpandRM1(feedback, "content",true);
		weightedTerms = ExpandRM3(weightedTerms);
		String complexQuery = getComplexQuery(weightedTerms);
		double[] scores = normalize(RetrievalController.rerankResults(docids, indexName, complexQuery,"content"));
		
		for (int i = 0; i < scores.length; i++) {
			relevance[i] = scores[i];
		}
	}
	
	private String getComplexQuery(List<WeightedTerm> weightTerms){
		String q = "";
		for (int i = 0; i < weightTerms.size(); i++) {
			q += weightTerms.get(i).term + "^" + String.format("%.6f", weightTerms.get(i).score ) + " ";
		}
		return q;
	}
	
	
	private Map<String,Double> queryLikehood(String query){
		Map<String,Double> probs = new HashMap<String,Double>();
		List<String> terms = tokenizeString(RetrievalController.getAnalyzer(), query);
		double size = terms.size();
		for (String term : terms) {
			probs.putIfAbsent(term, 0d);
			probs.put(term, probs.get(term)+1);
		}
		for (String term : probs.keySet()) {
			probs.put(term, probs.get(term)/size);
		}
		return probs;
	}
	
	private List<WeightedTerm> ExpandRM3(List<WeightedTerm> weightedTerms) {
		List<WeightedTerm> newWeightedTerms = new ArrayList<WeightedTerm>();
		Map<String,Double> probs = queryLikehood(query);
		
		float normFactor = 0;
		for (int i = 0; i < weightedTerms.size(); i++) {
			if (probs.containsKey(weightedTerms.get(i).term)){
				weightedTerms.get(i).score = (1-lambda)*probs.get(weightedTerms.get(i).term) + lambda*weightedTerms.get(i).score;
				
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
		Map<String,Double> queryCounts = extractCounts(query);
		
		List<Map<String,Double>> newTermCounts = new ArrayList<Map<String,Double>>();
 		// for each document and passage in feedback, compute counts
		for (int i = 0; i < feedback.length; i++) {
			if (!addFeedback){
				break;
			}
			if (!feedback[i].isOnTopic()){
				continue;
			}
			Passage[] passages = feedback[i].getPassages();
			for (int j = 0; j < passages.length; j++) {
				 Map<String,Double> counts = extractCounts(passages[j].getText());
				 newTermCounts.add(counts);
				 docLens.add(getDocLen(counts));
				 relevances.add((double) passages[j].getRelevance());
			}
		}
		
		termCounts.addAll(newTermCounts);
		
		// compute QLE for each passage
		for (Map<String,Double> passageCounts : newTermCounts) {
			double qle = queryLikehood(queryCounts,passageCounts,"content");
			queryLikelihoodForPassages.get("content").add(qle);
			qle = queryLikehood(queryCounts,passageCounts,"title");
			queryLikelihoodForPassages.get("title").add(qle);
		}
		
		
		List<WeightedTerm> weightedTerms = new ArrayList<WeightedTerm>();
		
		double sumTotalWeights = 0;
		for (String  term: terms) {
			double weight = 0;
			for (int i = 0; i < termCounts.size(); ++i) {
				double docLen = docLens.get(i);
				weight += relevances.get(i)*queryLikehoodTerm(term,termCounts.get(i).getOrDefault(term,0d), docLen,field)*queryLikelihoodForPassages.get(field).get(i) ;
			}
			weightedTerms.add(new WeightedTerm(term, weight));
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
	
	private double queryLikehoodTerm(String term, double termCount, double docLen, String field){
		double score = 0;
		score = termCount + muFB*collectionProbability(term,field);
		if (score == 0) {
			return 0;
		}
		score /= (docLen + muFB);
		return score;
	}
	
	private double queryLikehood(Map<String,Double> queryCounts, Map<String,Double> passageCounts, String field){
		double score = 1;
		double docLen = 0;
		for (double v : passageCounts.values()) {
			docLen += v;
		}
		for (Entry<String,Double> entry : queryCounts.entrySet()) {
			double s = passageCounts.getOrDefault(entry.getKey(), 0d) + mu*collectionProbability(entry.getKey(), field);
			s /= (docLen + mu);
			score *= s;
		}
		return score;
	}
	
	private double collectionProbability(String term, String field) {
		double ttf = RetrievalController.termStatistics.get(indexName + "_" + field).totalTermFreq(term);
		double sttf = RetrievalController.sumTotalTerms.get(indexName + "_" + field);
		return ttf/sttf;
	}
	
	private double getDocLen(Map<String,Double> counts){
		double docLen = 0;
		for (double v : counts.values()) {
			docLen += v;
		}
		return docLen;
	}

	private Map<String,Double> extractCounts(String text){
		List<String> grams = tokenizeString(RetrievalController.getAnalyzer(), text);
		Map<String,Double> counts = new HashMap<String,Double>();
		for (String gram : grams) {
			terms.add(gram);
			double count = counts.getOrDefault(gram, 0d);
			counts.put(gram, count+1);
		}
		return counts;
	}
	
	public List<String> tokenizeString(Analyzer analyzer, String str) {
		List<String> result = new ArrayList<String>();
		try {
		      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
		      stream.reset();
	
		      while (stream.incrementToken()) {
		    	  result.add(stream.getAttribute(CharTermAttribute.class).toString());
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
		    public String term;
		    public double score;

		    public WeightedTerm(String term, double score) {
		    	this.score = score;
		      
		    	this.term = term;
		    }


		    public String getTerm() {
		      return term;
		    }
		    

		    public int compareTo(WeightedTerm other) {
		      int result = -Double.compare(this.score, other.score);
		      if (result != 0) {
		        return result;
		      }
		      result = (this.term.compareTo(other.term));
		      return result;
		    }

		    @Override
		    public String toString() {
		      return "<" + term + "," + score + ">";
		    }
		  }

}
