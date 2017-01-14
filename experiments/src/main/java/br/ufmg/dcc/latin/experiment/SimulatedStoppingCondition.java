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

public class SimulatedStoppingCondition {

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
	    Evaluator.trecUser = trecUser;
	    FileWriter fw = new FileWriter( "SimulatedStoppingCondition_" + args[0] + ".txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		
		FileWriter fwS = new FileWriter( "SimulatedStoppedAt_" + args[0] + ".txt");
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
		
		    
					 
			   
		   FeedbackModeling xQuADfeedbackModeling0 = new FeedbackModeling();
		   xQuADfeedbackModeling0.trecUser = trecUser;
		   InteractiveReranker xQuADReranker0 = new xQuAD(xQuADfeedbackModeling0);
		   xQuADReranker0.setStopCondition("S0");
		   xQuADReranker0.start(baselineResultSet, new double[]{0.5});
		   String[][] xQuADAcc0 = new String[50][];
		   
		   FeedbackModeling xQuADfeedbackModeling1 = new FeedbackModeling();
		   xQuADfeedbackModeling1.trecUser = trecUser;
		   InteractiveReranker xQuADReranker1 = new xQuAD(xQuADfeedbackModeling1);
		   xQuADReranker1.setStopCondition("S1");
		   xQuADReranker1.start(baselineResultSet, new double[]{0.5});
		   String[][] xQuADAcc1 = new String[50][];
		   
		   FeedbackModeling xQuADfeedbackModeling2 = new FeedbackModeling();
		   xQuADfeedbackModeling2.trecUser = trecUser;
		   InteractiveReranker xQuADReranker2 = new xQuAD(xQuADfeedbackModeling2);
		   xQuADReranker2.setStopCondition("S2");
		   xQuADReranker2.start(baselineResultSet, new double[]{0.5});
		   String[][] xQuADAcc2 = new String[50][];
		   
		   FeedbackModeling xQuADfeedbackModeling3 = new FeedbackModeling();
		   xQuADfeedbackModeling3.trecUser = trecUser;
		   InteractiveReranker xQuADReranker3 = new xQuAD(xQuADfeedbackModeling3);
		   xQuADReranker3.setStopCondition("S3");
		   xQuADReranker3.start(baselineResultSet, new double[]{0.5});
		   String[][] xQuADAcc3 = new String[50][];
		   
		   FeedbackModeling PM2feedbackModeling0 = new FeedbackModeling();
		   PM2feedbackModeling0.trecUser = trecUser;
		   InteractiveReranker PM2Reranker0 = new PM2(PM2feedbackModeling0);
		   PM2Reranker0.setStopCondition("S0");
		   PM2Reranker0.start(baselineResultSet, new double[]{0.5});
		   String[][] PM2Acc0 = new String[50][];
		   
		   FeedbackModeling PM2feedbackModeling1 = new FeedbackModeling();
		   PM2feedbackModeling1.trecUser = trecUser;
		   InteractiveReranker PM2Reranker1 = new PM2(PM2feedbackModeling1);
		   PM2Reranker1.setStopCondition("S1");
		   PM2Reranker1.start(baselineResultSet, new double[]{0.5});
		   String[][] PM2Acc1 = new String[50][];
		   
		   FeedbackModeling PM2feedbackModeling2 = new FeedbackModeling();
		   PM2feedbackModeling2.trecUser = trecUser;
		   InteractiveReranker PM2Reranker2 = new PM2(PM2feedbackModeling2);
		   PM2Reranker2.setStopCondition("S2");
		   PM2Reranker2.start(baselineResultSet, new double[]{0.5});
		   String[][] PM2Acc2 = new String[50][];
		   
		   FeedbackModeling PM2feedbackModeling3 = new FeedbackModeling();
		   PM2feedbackModeling3.trecUser = trecUser;
		   InteractiveReranker PM2Reranker3 = new PM2(PM2feedbackModeling3);
		   PM2Reranker3.setStopCondition("S3");
		   PM2Reranker3.start(baselineResultSet, new double[]{0.5});
		   String[][] PM2Acc3 = new String[50][];
		
		   FeedbackModeling baselinefeedbackModeling0 = new FeedbackModeling();
		   baselinefeedbackModeling0.trecUser = trecUser;
		   InteractiveReranker baselineReranker0 = new Baseline(baselinefeedbackModeling0);
		   baselineReranker0.setStopCondition("S0");
		   baselineReranker0.start(baselineResultSet, new double[]{0.5});
		   String[][] baselineAcc0 = new String[50][];
		   
		   FeedbackModeling baselinefeedbackModeling1 = new FeedbackModeling();
		   baselinefeedbackModeling1.trecUser = trecUser;
		   InteractiveReranker baselineReranker1 = new Baseline(baselinefeedbackModeling1);
		   baselineReranker1.setStopCondition("S1");
		   baselineReranker1.start(baselineResultSet, new double[]{0.5});
		   String[][] baselineAcc1 = new String[50][];
		   
		   FeedbackModeling baselinefeedbackModeling2 = new FeedbackModeling();
		   baselinefeedbackModeling2.trecUser = trecUser;
		   InteractiveReranker baselineReranker2 = new Baseline(baselinefeedbackModeling2);
		   baselineReranker2.setStopCondition("S2");
		   baselineReranker2.start(baselineResultSet, new double[]{0.5});
		   String[][] baselineAcc2 = new String[50][];
		   
		   FeedbackModeling baselinefeedbackModeling3 = new FeedbackModeling();
		   baselinefeedbackModeling3.trecUser = trecUser;
		   InteractiveReranker baselineReranker3 = new Baseline(baselinefeedbackModeling3);
		   baselineReranker3.setStopCondition("S3");
		   baselineReranker3.start(baselineResultSet, new double[]{0.5});
		   String[][] baselineAcc3 = new String[50][];
		   
		   ResultSet resultSet = null;
		   Feedback[] feedbacks = null;

		   trecUser.generateSubtopics(baselineResultSet.docnos);
		   
		   for (int i = 0; i < 100; i++) {
			  
    			resultSet = xQuADReranker0.get();
    			xQuADAcc0[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			xQuADReranker0.update(feedbacks);
    			
    			resultSet = xQuADReranker1.get();
    			xQuADAcc1[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			xQuADReranker1.update(feedbacks);
    			
    			resultSet = xQuADReranker2.get();
    			xQuADAcc2[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			xQuADReranker2.update(feedbacks);
    			
    			resultSet = xQuADReranker3.get();
    			xQuADAcc3[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			xQuADReranker3.update(feedbacks);
    			
    			
    			resultSet = PM2Reranker0.get();
    			PM2Acc0[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			PM2Reranker0.update(feedbacks);
    			
    			resultSet = PM2Reranker1.get();
    			PM2Acc1[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			PM2Reranker1.update(feedbacks);
    			
    			resultSet = PM2Reranker2.get();
    			PM2Acc2[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			PM2Reranker2.update(feedbacks);
    			
    			resultSet = PM2Reranker3.get();
    			PM2Acc3[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			PM2Reranker3.update(feedbacks);
    			
    			resultSet = baselineReranker0.get();
    			baselineAcc0[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			baselineReranker0.update(feedbacks);
    		
    			resultSet = baselineReranker1.get();
    			baselineAcc1[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			baselineReranker1.update(feedbacks);
    			
    			resultSet = baselineReranker2.get();
    			baselineAcc2[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			baselineReranker2.update(feedbacks);
    			
    			resultSet = baselineReranker3.get();
    			baselineAcc3[i] = resultSet.docnos;
    			feedbacks = trecUser.get(resultSet);
    			baselineReranker3.update(feedbacks);
    			
        		double gainxQuAD0 = cubeTest.getGain(i+1, topicId, xQuADAcc0);
        		double gainxPM20 = cubeTest.getGain(i+1, topicId, PM2Acc0);
        		double gainbaseline0 = cubeTest.getGain(i+1, topicId, baselineAcc0);

        		double gainxQuAD1 = cubeTest.getGain(i+1, topicId, xQuADAcc1);
        		double gainxPM21 = cubeTest.getGain(i+1, topicId, PM2Acc1);
        		double gainbaseline1 = cubeTest.getGain(i+1, topicId, baselineAcc1);
        		
        		
        		double gainxQuAD2 = cubeTest.getGain(i+1, topicId, xQuADAcc2);
        		double gainxPM22 = cubeTest.getGain(i+1, topicId, PM2Acc2);
        		double gainbaseline2 = cubeTest.getGain(i+1, topicId, baselineAcc2);
        		
        		
        		double gainxQuAD3 = cubeTest.getGain(i+1, topicId, xQuADAcc3);
        		double gainxPM23 = cubeTest.getGain(i+1, topicId, PM2Acc3);
        		double gainbaseline3 = cubeTest.getGain(i+1, topicId, baselineAcc3);
        		
        		
        		double actxQuAD0 = cubeTest.getAverageCubeTest(i+1, topicId, xQuADAcc0);
        		double actxPM20 = cubeTest.getAverageCubeTest(i+1, topicId, PM2Acc0);
        		double actbaseline0 = cubeTest.getAverageCubeTest(i+1, topicId, baselineAcc0);

        		double actxQuAD1 = cubeTest.getAverageCubeTest(i+1, topicId, xQuADAcc1);
        		double actxPM21 = cubeTest.getAverageCubeTest(i+1, topicId, PM2Acc1);
        		double actbaseline1 = cubeTest.getAverageCubeTest(i+1, topicId, baselineAcc1);
        		
        		
        		double actxQuAD2 = cubeTest.getAverageCubeTest(i+1, topicId, xQuADAcc2);
        		double actxPM22 = cubeTest.getAverageCubeTest(i+1, topicId, PM2Acc2);
        		double actbaseline2 = cubeTest.getAverageCubeTest(i+1, topicId, baselineAcc2);
        		
        		
        		double actxQuAD3 = cubeTest.getAverageCubeTest(i+1, topicId, xQuADAcc3);
        		double actxPM23 = cubeTest.getAverageCubeTest(i+1, topicId, PM2Acc3);
        		double actbaseline3 = cubeTest.getAverageCubeTest(i+1, topicId, baselineAcc3);
        		
        		out.println(topicId + "  " + (i+1) 
        				+ " " + gainxQuAD0 + " " +gainxPM20 + " "  + gainbaseline0 
        				+ " " + gainxQuAD1 + " " +gainxPM21 + " "  + gainbaseline1
        				+ " " + gainxQuAD2 + " " +gainxPM22 + " "  + gainbaseline2
        				+ " " + gainxQuAD3 + " " +gainxPM23 + " "  + gainbaseline3
        				+ " " + actxQuAD0 + " " +actxPM20 + " "  + actbaseline0 
        				+ " " + actxQuAD1 + " " +actxPM21 + " "  + actbaseline1
        				+ " " + actxQuAD2 + " " +actxPM22 + " "  + actbaseline2
        				+ " " + actxQuAD3 + " " +actxPM23 + " "  + actbaseline3);
        	
			}
			   
			outS.println(topicId + " " +xQuADReranker2.stoppedAt + " " + xQuADReranker3.stoppedAt 
					+ " " + PM2Reranker2.stoppedAt + " " + PM2Reranker3.stoppedAt
					+ " " + baselineReranker2.stoppedAt + " " + baselineReranker3.stoppedAt);
		   
    
			
	    }
		br.close();
		out.close();
		outS.close();
		
		
	}


}
