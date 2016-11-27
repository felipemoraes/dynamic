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
	
	List<Map<String,Float>> termCounts;
	List<Float> relevances;
	List<Float> docLens;
	Set<String> terms;
	Map<String, List<Float>> queryLikelihoodForPassages;
	String query;
	String indexName;
	
	float mu;
	float muFB;
	float lambda;
	int numberOfTerms;

	@Override
	protected float score(int docid) {
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
		termCounts = new ArrayList<Map<String,Float>>();
		relevances = new ArrayList<Float>();
		docLens = new ArrayList<Float>();
		terms = new HashSet<String>();
		queryLikelihoodForPassages = new HashMap<String, List<Float>>();
		queryLikelihoodForPassages.put("content", new ArrayList<Float>());
		queryLikelihoodForPassages.put("title", new ArrayList<Float>());
	}
	
	@Override
	public void start(String query, String index){
		super.start(query,index);
		this.query = query;
		this.indexName = index;
		RetrievalController.loadDocFreqs(indexName);
	}
	
	@Override
	public void update(Feedback[] feedback) {
		
		List<WeightedTerm> weightedTerms = ExpandRM1(feedback, "content",true);
		weightedTerms = ExpandRM3(weightedTerms);
		String complexQuery = getComplexQuery(weightedTerms);
		float[] scoresContent = normalize(RetrievalController.rerankResults(docids, indexName, complexQuery,"content"));
		
		weightedTerms = ExpandRM1(feedback, "title",false);
		weightedTerms = ExpandRM3(weightedTerms);
		complexQuery = getComplexQuery(weightedTerms);
		float[] fieldWeights = RetrievalController.getFiedlWeights();
		float[] scoresTitle = normalize(RetrievalController.rerankResults(docids, indexName, complexQuery,"title"));
		for (int i = 0; i < scoresTitle.length; i++) {
			relevance[i] = fieldWeights[0]*scoresContent[i] + fieldWeights[1]*scoresTitle[i];
		}
	}
	
	private String getComplexQuery(List<WeightedTerm> weightTerms){
		String q = "";
		for (int i = 0; i < weightTerms.size(); i++) {
			q += weightTerms.get(i).term + "^" + String.format("%.6f", weightTerms.get(i).score ) + " ";
		}
		return q;
	}
	
	
	private Map<String,Float> queryLikehood(String query){
		Map<String,Float> probs = new HashMap<String,Float>();
		List<String> terms = tokenizeString(RetrievalController.getAnalyzer(), query);
		float size = terms.size();
		for (String term : terms) {
			probs.putIfAbsent(term, 0f);
			probs.put(term, probs.get(term)+1);
		}
		for (String term : probs.keySet()) {
			probs.put(term, probs.get(term)/size);
		}
		return probs;
	}
	
	private List<WeightedTerm> ExpandRM3(List<WeightedTerm> weightedTerms) {
		List<WeightedTerm> newWeightedTerms = new ArrayList<WeightedTerm>();
		Map<String,Float> probs = queryLikehood(query);
		
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
		Map<String,Float> queryCounts = extractCounts(query);
		
		List<Map<String,Float>> newTermCounts = new ArrayList<Map<String,Float>>();
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
				 Map<String,Float> counts = extractCounts(passages[j].getText());
				 newTermCounts.add(counts);
				 docLens.add(getDocLen(counts));
				 relevances.add((float) passages[j].getRelevance());
			}
		}
		termCounts.addAll(newTermCounts);
		
		// compute QLE for each passage
		for (Map<String,Float> passageCounts : newTermCounts) {
			float qle = queryLikehood(queryCounts,passageCounts,"content");
			queryLikelihoodForPassages.get("content").add(qle);
			qle = queryLikehood(queryCounts,passageCounts,"title");
			queryLikelihoodForPassages.get("title").add(qle);
		}
		
		
		List<WeightedTerm> weightedTerms = new ArrayList<WeightedTerm>();
		
		float sumTotalWeights = 0;
		for (String  term: terms) {
			float weight = 0;
			for (int i = 0; i < termCounts.size(); ++i) {
				float docLen = docLens.get(i);
				weight += relevances.get(i)*queryLikehoodTerm(term,termCounts.get(i).getOrDefault(term,0f), docLen,field)*queryLikelihoodForPassages.get(field).get(i) ;
			}
			weightedTerms.add(new WeightedTerm(term, weight));
			sumTotalWeights += weight;
		}
		
		for (int i = 0; i < weightedTerms.size(); i++) {
			weightedTerms.get(i).score /= sumTotalWeights;
		}
		
		return weightedTerms;
	}
	
	private float queryLikehoodTerm(String term, float termCount, float docLen, String field){
		float score = 0;
		score = termCount + muFB*collectionProbability(term,field);
		score /= (docLen + muFB);
		return score;
	}
	
	private float queryLikehood(Map<String,Float> queryCounts, Map<String,Float> passageCounts, String field){
		float score = 1;
		float docLen = 0;
		for (float v : passageCounts.values()) {
			docLen += v;
		}
		for (Entry<String,Float> entry : queryCounts.entrySet()) {
			float s = passageCounts.getOrDefault(entry.getKey(), 0f) + mu*collectionProbability(entry.getKey(), field);
			s /= (docLen + mu);
			score *= s;
		}
		return score;
	}
	
	private float collectionProbability(String term, String field) {
		float ttf = RetrievalController.termStatistics.get(indexName + "_" + field).totalTermFreq(term);
		float sttf = RetrievalController.sumTotalTerms.get(indexName + "_" + field);
		return ttf/sttf;
	}
	
	private float getDocLen(Map<String,Float> counts){
		float docLen = 0;
		for (float v : counts.values()) {
			docLen += v;
		}
		return docLen;
	}

	private Map<String,Float> extractCounts(String text){
		List<String> grams = tokenizeString(RetrievalController.getAnalyzer(), text);
		Map<String,Float> counts = new HashMap<String,Float>();
		for (String gram : grams) {
			terms.add(gram);
			float count = counts.getOrDefault(gram, 0f);
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
		    public float score;

		    public WeightedTerm(String term, float score) {
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
