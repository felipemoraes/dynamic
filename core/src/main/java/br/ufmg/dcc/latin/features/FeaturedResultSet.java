/**
 * 
 */
package br.ufmg.dcc.latin.features;

import java.util.Map.Entry;

import org.apache.lucene.search.similarities.BasicStats;


import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.matching.Scorer;



/**
 * @author Felipe Moraes
 *
 */


public class FeaturedResultSet {
	

	
	private int[] docIds;
	private float[][] scores;
	
	public FeaturedResultSet(ResultSet resultSet, FeaturesService featuresService, String[] fields) {

		docIds = resultSet.getDocIds();
		
		scores = new float[docIds.length][];
		
		
		for (int i = 0; i < docIds.length; i++) {
			float[] qiFeatures = featuresService.getQueryIndependentFeatures(docIds[i]);
			
			scores[i] = new float[featuresService.getScorers().size()*fields.length + qiFeatures.length];
			int j = 0;
			for (int m = 0; m < fields.length; m++) {
				
				int[] termFrequency = resultSet.getPostings()[i][m].getTermFrequency();
				long[] docFrequency = resultSet.getPostings()[i][m].getDocFrequency();
				long[] totalTermFrequency = resultSet.getPostings()[i][m].getTotalTermFrequency();
				String[] terms= resultSet.getPostings()[i][m].getTerms();
				long docCount  = resultSet.getPostings()[i][m].getDocCount();
				long docLen = resultSet.getPostings()[i][m].getDocLen();
				long sumTotalTermFrequency = resultSet.getPostings()[i][m].getSumTotalTermFrequency();
				float avgFieldLength = (float) sumTotalTermFrequency/ (float) docCount;
				int n = terms.length;
				
				BasicStats[] basicStats = buildBasicStats(docFrequency, totalTermFrequency, terms, docCount,
						sumTotalTermFrequency, avgFieldLength, n);
			
				for (Entry<String, Scorer> scorer : featuresService.getScorers().entrySet()) { 
		
					float s = scorer.getValue()
							.totalScore(basicStats, termFrequency, docLen, n);
					scores[i][j] = s;
					j++;
				}
			}
			for (int k = j; k < qiFeatures.length; k++) {
				scores[i][k] = qiFeatures[k];
			}
		}
	}


	private BasicStats[] buildBasicStats(long[] docFrequency, long[] totalTermFrequency, String[] terms, long docCount,
			long sumTotalTermFrequency, float avgFieldLength, int n) {
		BasicStats[] basicStats = new BasicStats[n];
		for (int j = 0; j < n; j++) {
			basicStats[j] = new BasicStats(terms[j]);
			basicStats[j].setDocFreq(docFrequency[j]);
			basicStats[j].setTotalTermFreq(totalTermFrequency[j]);
			basicStats[j].setNumberOfDocuments(docCount);
			basicStats[j].setNumberOfFieldTokens(sumTotalTermFrequency);
			basicStats[j].setAvgFieldLength(avgFieldLength);
		}
		return basicStats;
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
