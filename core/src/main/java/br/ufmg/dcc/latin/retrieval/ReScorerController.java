package br.ufmg.dcc.latin.retrieval;


import java.io.IOException;
import java.io.StringReader;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.search.similarities.BoostedBasicStats;
import org.apache.lucene.search.similarities.LMSimilarity.LMStats;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.index.DocVec;
import br.ufmg.dcc.latin.index.InMemoryVocabulary;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class ReScorerController {

	
	public static double[] sim(int[] docids, int docid){
		
		int n = RetrievalCache.docids.length;
		
		double[] scores = new double[n];
		int count = (int) RetrievalController.directedIndex[0].getDocCount();
		
		for (int i = 0; i < docids.length; i++) {
			scores[i] = tfidf(i,docid,count,RetrievalController.vocab[0]);
		}
		
	    return scores;
	}
	
	public static TIntDoubleHashMap getComplexQuery(String query){
		TIntArrayList terms = tokenizeString(RetrievalController.getAnalyzer(), query);
		TIntDoubleHashMap queryProb = new TIntDoubleHashMap();
		for (int i = 0; i < terms.size(); i++) {
			if (queryProb.contains(terms.get(i))){
				queryProb.put(terms.get(i), queryProb.get(i)+1);
			} else {
				queryProb.put(terms.get(i),1);
			}
		}
		
		int[] uniqTerms = queryProb.keys();
		
		for (int i = 0; i < uniqTerms.length ; i++) {	
			queryProb.put(uniqTerms[i], queryProb.get(uniqTerms[i])/(float) terms.size());
		}
		
		return queryProb;
	}
	
	public static double[] rescore(TIntDoubleHashMap complexQuery) {
		
		int n = RetrievalCache.docids.length;
		double[] scores = new double[n];
		int[] terms = complexQuery.keys();
		BoostedBasicStats stats = new BoostedBasicStats("");
		stats.setNumberOfDocuments(RetrievalController.directedIndex[0].getDocCount());
		
		for (int i = 0; i < terms.length; i++) {
		
			
			stats.setBoost((float) (complexQuery.get(terms[i])*RetrievalController.getFiedlWeights()[0]));
			stats.setDocFreq(RetrievalController.termStats[0].docFreq(terms[i]));
			stats.setTotalTermFreq(RetrievalController.termStats[0].totalTermFreq(terms[i]));
			stats.setNumberOfFieldTokens((long) RetrievalController.directedIndex[0].sumTotalTermsFreq());
			
			float collectionProbability = (stats.getTotalTermFreq()+1F);
			collectionProbability /= (stats.getNumberOfFieldTokens()+1F);
			
			stats.setCollectionProbability(collectionProbability);
			double sttf = ((Number) RetrievalController.directedIndex[0].sumTotalTermsFreq()).doubleValue();
			double avgFieldLength  = sttf / (double) RetrievalController.directedIndex[0].getDocCount();
			stats.setAvgFieldLength((float) avgFieldLength);
			
			for (int j = 0; j < scores.length; j++) {
				
				int freq = RetrievalController.directedIndex[0].docVecs[j].getFreq(terms[i]);
				int docLen = (int) RetrievalController.directedIndex[0].docVecs[j].docLen();
				
				if (freq > 0) {
					scores[j] += RetrievalController.similarity.score(stats, freq, docLen);
				}
			}

			stats.setBoost((float) (complexQuery.get(terms[i])*RetrievalController.getFiedlWeights()[1]));
			stats.setDocFreq(RetrievalController.termStats[1].docFreq(terms[i]));
			stats.setTotalTermFreq(RetrievalController.termStats[1].totalTermFreq(terms[i]));
			stats.setNumberOfFieldTokens((long) RetrievalController.directedIndex[1].sumTotalTerms());
			sttf = ((Number) RetrievalController.directedIndex[1].sumTotalTermsFreq()).doubleValue();
			
			avgFieldLength  = sttf / (double) RetrievalController.directedIndex[1].getDocCount();
			stats.setAvgFieldLength((float) avgFieldLength);
			
			collectionProbability = (stats.getTotalTermFreq()+1F);
			collectionProbability /= (stats.getNumberOfFieldTokens()+1F);
			stats.setCollectionProbability(collectionProbability);
			
			
			for (int j = 0; j < scores.length; j++) {
				int freq = RetrievalController.directedIndex[1].docVecs[j].getFreq(terms[i]);
				int docLen = (int) RetrievalController.directedIndex[1].docVecs[j].docLen();
				if (freq > 0) {
					scores[j] += RetrievalController.similarity.score(stats, freq, docLen);
				}
			}
		}
		
		return scores;
	}
	
	public static TIntArrayList tokenizeString(Analyzer analyzer, String str) {
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

	
	
	private static double tfidf(int docid1, int docid2, int docCount, InMemoryVocabulary vocab){
		double score = 0;
		
		DocVec doc1 = RetrievalController.directedIndex[0].docVecs[docid1];
		DocVec doc2 = RetrievalController.directedIndex[0].docVecs[docid2];
	
		if (doc1 == null || doc2 == null ){
			return 0;
		}
		
		double docNorm1 = 0;
		double docNorm2 = 0;


		int[] terms = doc1.getTerms().length < doc2.getTerms().length ? doc1.getTerms() : doc2.getTerms();

		for (int i = 0; i < terms.length; i++) {
			
			double tf1 = doc1.getFreq(terms[i]);
			double tf2 = doc2.getFreq(terms[i]);
			double df = RetrievalController.termStats[0].docFreq(terms[i]);
			double idf = (double)(Math.log(docCount)/(df+1));
			double weight1 = tf1*idf;
			double weight2 = tf2*idf;
			docNorm1 += weight1*weight1;
			docNorm2 += weight2*weight2;
			
			score += weight1*weight2;
			
		}

		docNorm1 = (double) Math.sqrt(docNorm1);
		docNorm2 = (double) Math.sqrt(docNorm2);


		score /= (docNorm1*docNorm2);
		
		return score;
	}
	
	
	public static double getIdf(String field, int termId){
		
		int count = (int) RetrievalController.directedIndex[0].getDocCount();		
		double df = RetrievalController.termStats[0].docFreq(termId);
			
		double idf = (float) (Math.log(count)/(df+1));
		return idf;
	}
	
	
}
