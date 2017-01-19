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
import br.ufmg.dcc.latin.user.TrecUser;

public class StoppingConditionOracle {
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
	    BaselineRanker baselineRanker = getBaselineRanker("DPH");

	    TrecUser trecUser = TrecUser.getInstance("../share/truth_data.txt");
	    Evaluator.trecUser = trecUser;
	    FileWriter fw = new FileWriter( "SimulatedStoppedAt.txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		
		FileWriter fwS = new FileWriter( "St.txt");
	    BufferedWriter bwS = new BufferedWriter(fwS);
		PrintWriter outS = new PrintWriter(bwS);
	    
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",3);
	    	
	    	String topicId = splitLine[1];
	    	
	    	//if (!topicId.equals("DD16-1")){
	    	//	continue;
	    	//}
	    	System.out.println(topicId);
	    	trecUser.topicId = topicId;
			String query = splitLine[2].replaceAll("/", " ");
			String index = splitLine[0];
			ResultSet baselineResultSet = baselineRanker.search(query, index);
			
	    	for (int noise = 1; noise < 20; noise++ ) {
	    		
	    	
		    	for (int k = 0; k < 100; k++ ) {
				 
					   
				   FeedbackModeling xQuADfeedbackModeling0 = new FeedbackModeling();
				   xQuADfeedbackModeling0.trecUser = trecUser;
				   InteractiveReranker xQuADReranker0 = new xQuAD(xQuADfeedbackModeling0);
				   xQuADReranker0.setStopCondition("S4");
				   xQuADReranker0.noiseStop = (double) noise / 20.0;
				   xQuADReranker0.start(baselineResultSet, new double[]{0.5});
				   String[][] xQuADAcc0 = new String[100][];
				   
				 
				   
				   FeedbackModeling PM2feedbackModeling0 = new FeedbackModeling();
				   PM2feedbackModeling0.trecUser = trecUser;
				   InteractiveReranker PM2Reranker0 = new PM2(PM2feedbackModeling0);
				   PM2Reranker0.setStopCondition("S4");
				   PM2Reranker0.noiseStop = (double) noise / 20.0;
				   PM2Reranker0.start(baselineResultSet, new double[]{0.5});
				   String[][] PM2Acc0 = new String[100][];
				   
				
				
				   FeedbackModeling baselinefeedbackModeling0 = new FeedbackModeling();
				   baselinefeedbackModeling0.trecUser = trecUser;
				   InteractiveReranker baselineReranker0 = new Baseline(baselinefeedbackModeling0);
				   baselineReranker0.setStopCondition("S4");
				   baselineReranker0.noiseStop = (double) noise / 20.0;
				   baselineReranker0.start(baselineResultSet, new double[]{0.5});
				   String[][] baselineAcc0 = new String[100][];
				   
				   
				   ResultSet resultSet = null;
				   Feedback[] feedbacks = null;
		
				   trecUser.generateSubtopics(baselineResultSet.docnos);
				   
				   for (int i = 0; i < 100; i++) {
					  
		    			resultSet = xQuADReranker0.get();
		    			xQuADAcc0[i] = resultSet.docnos;
		    			feedbacks = trecUser.get(resultSet);
		    			xQuADReranker0.update(feedbacks);
		    			
		    			
		    			
		    			
		    			resultSet = PM2Reranker0.get();
		    			PM2Acc0[i] = resultSet.docnos;
		    			feedbacks = trecUser.get(resultSet);
		    			PM2Reranker0.update(feedbacks);
		    			
		    		
		    			resultSet = baselineReranker0.get();
		    			baselineAcc0[i] = resultSet.docnos;
		    			feedbacks = trecUser.get(resultSet);
		    			baselineReranker0.update(feedbacks);
		    		
		    		
		        		double gainxQuAD0 = cubeTest.getGain(i+1, topicId, xQuADAcc0);
		        		double gainxPM20 = cubeTest.getGain(i+1, topicId, PM2Acc0);
		        		double gainbaseline0 = cubeTest.getGain(i+1, topicId, baselineAcc0);
		        		
		        		double actxQuAD0 = cubeTest.getAverageCubeTest(i+1, topicId, xQuADAcc0);
		        		double actxPM20 = cubeTest.getAverageCubeTest(i+1, topicId, PM2Acc0);
		        		double actbaseline0 = cubeTest.getAverageCubeTest(i+1, topicId, baselineAcc0);
		
		        		
		        		out.println(topicId + "  " + (i+1) + " " + noise + " " + " " + k
		        				+ " " + gainxQuAD0 + " " +gainxPM20 + " "  + gainbaseline0 
		
		        				+ " " + actxQuAD0 + " " +actxPM20 + " "  + actbaseline0 );
		        	
					}
					   
				   outS.println(topicId + " " +xQuADReranker0.stoppedAt 
							+ " " + PM2Reranker0.stoppedAt + " "
							+ " " + baselineReranker0.stoppedAt);
				   
		    
					
			    }
	    	}
	    }
	    
		br.close();
		out.close();
		outS.close();
	}

}
