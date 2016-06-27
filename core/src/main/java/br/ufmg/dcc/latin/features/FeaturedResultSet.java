/**
 * 
 */
package br.ufmg.dcc.latin.features;

import java.util.Map.Entry;

import org.apache.lucene.search.similarities.BasicStats;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.matching.Scorer;



/**
 * @author Felipe Moraes
 *
 */


public class FeaturedResultSet {
	
	
	static Logger logger = LoggerFactory.getLogger(FeaturedResultSet.class);

	
	private int[] docIds;
	private float[][] scores;
	
	public FeaturedResultSet(ResultSet resultSet, FeaturesService featuresService) {

		docIds = resultSet.getDocIds();
		
		scores = new float[docIds.length][];
		
		
		for (int i = 0; i < docIds.length; i++) {
			
			int[] termFrequency = resultSet.getPostings()[i].getTermFrequency();
			long[] docFrequency = resultSet.getPostings()[i].getDocFrequency();
			long[] totalTermFrequency = resultSet.getPostings()[i].getTotalTermFrequency();
			String[] terms= resultSet.getPostings()[i].getTerms();
			long docCount  = resultSet.getPostings()[i].getDocCount();
			long docLen = resultSet.getPostings()[i].getDocLen();
			long sumTotalTermFrequency = resultSet.getPostings()[i].getSumTotalTermFrequency();
			float avgFieldLength = (float) sumTotalTermFrequency/ (float) docCount;
			int n = terms.length;
			
			BasicStats[] basicStats = new BasicStats[n];
			
			float[] qiFeatures = featuresService.getQueryIndependentFeatures(docIds[i]);
			
			scores[i] = new float[featuresService.getScorers().size() + qiFeatures.length];
			for (int j = 0; j < n; j++) {
				basicStats[j] = new BasicStats(terms[j]);
				basicStats[j].setDocFreq(docFrequency[j]);
				basicStats[j].setTotalTermFreq(totalTermFrequency[j]);
				basicStats[j].setNumberOfDocuments(docCount);
				basicStats[j].setNumberOfFieldTokens(sumTotalTermFrequency);
				basicStats[j].setAvgFieldLength(avgFieldLength);
			}
			
			int j = 0;
			System.out.println("docId " + docIds[i]);
			for (Entry<String, Scorer> scorer : featuresService.getScorers().entrySet()) { 
				float s = scorer.getValue()
						.totalScore(basicStats, termFrequency, docLen, n);
				System.out.println(resultSet.getScores()[i] + " " +s);
				scores[i][j] = s;
				j++;
			}
			System.out.println();
			System.out.println();
			for (int k = 0; k < qiFeatures.length; k++) {
				scores[i][j+k] = qiFeatures[k];
			}
			
		}
	}


	public float[][] getScores() {
		return scores;
	}



	public void setScores(float[][] scores) {
		this.scores = scores;
	}



	public int[] getDocIds() {
		return docIds;
	}



	public void setDocIds(int[] docIds) {
		this.docIds = docIds;
	}



}
