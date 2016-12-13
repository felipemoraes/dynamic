package br.ufmg.dcc.latin.aspect;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.queryparser.classic.QueryParser;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.diversity.FlatAspectModel;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.Passage;
import br.ufmg.dcc.latin.retrieval.ReScorerController;
import br.ufmg.dcc.latin.retrieval.RetrievalController;
import gnu.trove.map.hash.TIntDoubleHashMap;

public class SubtopicNameAspectMining extends AspectMining {

	
	private Map<String,String> subtopicsNames;
	
	
	private FlatAspectModel flatAspectModel;
	
	public SubtopicNameAspectMining(){
		
		n = RetrievalCache.docids.length;
		importance = new double[0];
		novelty = new double[0];
		coverage = new double[n][0];
		v = new double[0];
		s = new double[0];
		accumulatedRelevance = new double[0];
		
		readAspectsName();
	}
	
	
	private void readAspectsName() {
		subtopicsNames = new HashMap<String,String>();
		try (BufferedReader br = new BufferedReader(new FileReader("../share/subtopics_names.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split("\t",3);
		    	subtopicsNames.put(splitLine[1],splitLine[2]);
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}


	@Override
	public void sendFeedback(String index,String query,  Feedback[] feedbacks) {
		
		cacheFeedback(feedbacks);
		
		if (flatAspectModel == null) {
			flatAspectModel = new FlatAspectModel();
		}
		
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].isOnTopic()){
				continue;
			}
			Passage[] passages = feedbacks[i].getPassages();
			for (int j = 0; j < passages.length; j++) {
				flatAspectModel.addToAspect(passages[j].getAspectId(),RetrievalController.getPassage(passages[j].getPassageId()),passages[j].getRelevance());
			}
		}
		

		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			return;
		}
		importance = new double[aspectSize];
		novelty = new double[aspectSize];
		coverage = new double[n][aspectSize];
		accumulatedRelevance = new double[aspectSize];
		v = new double[aspectSize];
		s = new double[aspectSize];
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		Arrays.fill(v, 1.0f);
		Arrays.fill(s, 1.0f);
		
		int i = 0;
		for (String aspectId : flatAspectModel.getAspects()) {


		
			String aspectComponent = query + " " + subtopicsNames.get(aspectId);
			
			double[] scores = null;
			if (RetrievalCache.subtopicsCache.containsKey(aspectComponent)) {
				scores = RetrievalCache.subtopicsCache.get(aspectComponent);
			} else {
				TIntDoubleHashMap complexQuery = ReScorerController.getComplexQuery(aspectComponent);
				scores = ReScorerController.rescore(complexQuery);
			}
			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }

			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					float score = this.feedbacks[j].getRelevanceAspect(aspectId);
					coverage[j][i] = score;
				}
			}
			i++;
		}
		normalizeCoverage();
	}


	@Override
	public void miningFeedbackForCube(String query, String index, Feedback[] feedbacks) {
		cacheFeedback(feedbacks);
		if (flatAspectModel == null) {
			flatAspectModel = new FlatAspectModel();
		}
		
		for (int i = 0; i < feedbacks.length; i++) {
			if (!feedbacks[i].isOnTopic()){
				continue;
			}
			Passage[] passages = feedbacks[i].getPassages();
			for (int j = 0; j < passages.length; j++) {
				flatAspectModel.addToAspect(passages[j].getAspectId(), RetrievalController.getPassage(passages[j].getPassageId()) ,passages[j].getRelevance());
			}
		}

		
		int aspectSize = flatAspectModel.getAspects().size();
		if (aspectSize == 0) {
			return;
		}
		
		importance = new double[aspectSize];
		novelty = new double[aspectSize];
		coverage = new double[n][aspectSize];
		accumulatedRelevance = new double[aspectSize];
		
		
		float uniformImportance = 1.0f/aspectSize;
		
		Arrays.fill(importance, uniformImportance);
		Arrays.fill(novelty, 0f);
		Arrays.fill(accumulatedRelevance, 0f);
		int i = 0;

		for (String aspectId : flatAspectModel.getAspects()) {
	
			String aspectComponent = query + " " + subtopicsNames.get(aspectId);
			
			double[] scores = null;
			if (RetrievalCache.subtopicsCache.containsKey(aspectComponent)) {
				scores = RetrievalCache.subtopicsCache.get(aspectComponent);
			} else {
				TIntDoubleHashMap complexQuery = ReScorerController.getComplexQuery(aspectComponent);
				scores = ReScorerController.rescore(complexQuery);
			}
			scores = scaling(scores);
		    for(int j = 0;j< n ;++j) {
		    	coverage[j][i] = scores[j];
		    }

			for(int j = 0;j< n ;++j) {
				if (this.feedbacks[j] != null) {
					
					double score = this.feedbacks[j].getRelevanceAspect(aspectId);
					coverage[j][i] = score;
					
					if (score > 0) {
						novelty[i]++;
					}
					accumulatedRelevance[i] += score;
				}
			}
			i++;
		}
		
	}


	@Override
	public void updateAspects(String index) {
		// TODO Auto-generated method stub
		
	}
	

	
	

}
