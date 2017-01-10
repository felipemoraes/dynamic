package br.ufmg.dcc.latin.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.search.similarities.BM25;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.search.similarities.LMDirichlet;

import br.ufmg.dcc.latin.baseline.BaselineRanker;
import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.metrics.CubeTest;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.reranker.Baseline;
import br.ufmg.dcc.latin.reranker.InteractiveReranker;
import br.ufmg.dcc.latin.reranker.PM2;
import br.ufmg.dcc.latin.reranker.xQuAD;
import br.ufmg.dcc.latin.simulation.SimAP;
import br.ufmg.dcc.latin.user.TrecUser;

public class SimulatedNonRelevant {
	
	private static BaselineRanker getBaselineRanker(String ranker) {
		
		if (ranker.equals("DPH")) {
			return BaselineRanker.getInstance(new DPH(), new double[]{0.15,0.85});
		} else if (ranker.equals("LM")) {
			return BaselineRanker.getInstance(new LMDirichlet(2000f), new double[]{0.25,0.75});
		} else if (ranker.equals("BM25")) {
			return BaselineRanker.getInstance(new BM25(), new double[]{0,1});
		}
		return null;
		
	}

	public static void main(String[] args) throws IOException {
		CubeTest cubeTest = new CubeTest();
		
		String topicsFile = "../share/topics_domain.txt";
	
		
		BufferedReader br = new BufferedReader(new FileReader(topicsFile));
	    String line;
	    
	    BaselineRanker baselineRanker = getBaselineRanker(args[0]);
	    TrecUser trecUser = TrecUser.getInstance("../share/truth_data.txt");
	   
	    SimAP.trecUser = trecUser;
	   
	    
	    
	    FileWriter fw = new FileWriter( "SimulatedNonRelevant_" + args[0] + ".txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
	    		
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",3);
	    	
        	String topicId = splitLine[1];
        	 //if (!topicId.equals("DD16-1")){
        	//	continue;
        	// }
        	
        	System.out.println(topicId);
        	trecUser.topicId = topicId;
    		String query = splitLine[2].replaceAll("/", " ");
    		String index = splitLine[0];
    		ResultSet baselineResultSet = baselineRanker.search(query, index);
    		
    		int count = 0;
    		double[] relevances = trecUser.get(baselineResultSet.docnos);
    		for (double frac = 0; frac <= 1.01; frac+=0.05 ) {
    			
   				for (int k = 0; k < 20; k++) {
   					
					baselineResultSet = baselineRanker.search(frac,relevances);
					trecUser.generateSubtopics(baselineResultSet.docnos);
					
				    FeedbackModeling feedbackModeling = new FeedbackModeling();
				    feedbackModeling.trecUser = trecUser;
				    InteractiveReranker reranker = new xQuAD(feedbackModeling);
					reranker.start(baselineResultSet, new double[]{0.5});
	    			String[][] accResult = new String[10][];
	        		for (int i = 0; i < 10; i++) {
	        			ResultSet resultSet = reranker.get();
	        			accResult[i] = resultSet.docnos;
	        			Feedback[] feedbacks = trecUser.get(resultSet);
	        			reranker.update(feedbacks);
	    			}
	        		double actxQuAD = cubeTest.getAverageCubeTest(2, topicId, accResult);
	        		
	        		
				    feedbackModeling = new FeedbackModeling();
				    feedbackModeling.trecUser = trecUser;
				    reranker = new PM2(feedbackModeling);
					reranker.start(baselineResultSet, new double[]{0.5});
					accResult = new String[10][];
		    		for (int i = 0; i < 10; i++) {
		    			ResultSet resultSet = reranker.get();
		    			accResult[i] = resultSet.docnos;
		    			Feedback[] feedbacks = trecUser.get(resultSet);
		    			reranker.update(feedbacks);
					}
		    		
		    		double actxPM2 = cubeTest.getAverageCubeTest(2, topicId, accResult);
		    			
				    feedbackModeling = new FeedbackModeling();
				    feedbackModeling.trecUser = trecUser;
				    reranker = new Baseline(feedbackModeling);
					reranker.start(baselineResultSet, new double[]{0.5});
	    			accResult = new String[10][];
	        		for (int i = 0; i < 10; i++) {
	        			ResultSet resultSet = reranker.get();
	        			accResult[i] = resultSet.docnos;
	        			Feedback[] feedbacks = trecUser.get(resultSet);
	        			reranker.update(feedbacks);
	    			}
	        		
	        		
	        		double actbaseline = cubeTest.getAverageCubeTest(2, topicId, accResult);
	        		
	        		out.println(topicId + " " + frac  + " " + k + " " +actxQuAD + " " + actxPM2 + " " +actbaseline  );
	        		count++;
	        		if (count % 100 == 0) {
	        			System.out.println(count);
	        		}
   				}
    		}	
	    }
		br.close();
		out.close();

	}

}
