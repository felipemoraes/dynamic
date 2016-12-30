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
import br.ufmg.dcc.latin.reranker.xMMR;
import br.ufmg.dcc.latin.reranker.xQuAD;
import br.ufmg.dcc.latin.user.TrecUser;

public class SimulatedDynamicNoisedDiversity {
	
	private static BaselineRanker getBaselineRanker(String ranker) {
		
		if (ranker.equals("DPH")) {
			return BaselineRanker.getInstance(new DPH(), new double[]{0.15,0.85});
		} else if (ranker.equals("LM")) {
			return BaselineRanker.getInstance(new LMDirichlet(2500f), new double[]{0.25,0.75});
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
	    FileWriter fw = new FileWriter( "SimulatedDynamicNoisedDiversity_" + args[0] + ".txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
	    
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
			int count = 0;
		    for (int k = 0; k < 100; k++) {
			   double epsilon = 1;
					
			   
			   FeedbackModeling xQuADfeedbackModeling = new FeedbackModeling();
			   xQuADfeedbackModeling.trecUser = trecUser;
			   InteractiveReranker xQuADReranker = new xQuAD(xQuADfeedbackModeling);
			   xQuADReranker.start(baselineResultSet, new double[]{0.5});
			   String[][] xQuADAcc = new String[10][];
			   
			   FeedbackModeling PM2feedbackModeling = new FeedbackModeling();
			   PM2feedbackModeling.trecUser = trecUser;
			   InteractiveReranker PM2Reranker = new PM2(PM2feedbackModeling);
			   PM2Reranker.start(baselineResultSet, new double[]{0.5});
			   String[][] PM2Acc = new String[10][];
			
			   FeedbackModeling xMMRfeedbackModeling = new FeedbackModeling();
			   xMMRfeedbackModeling.trecUser = trecUser;
			   InteractiveReranker xMMRReranker = new xMMR(xMMRfeedbackModeling);
			   xMMRReranker.start(baselineResultSet, new double[]{0.5});
			   String[][] xMMRAcc = new String[10][];
	
			   FeedbackModeling baselinefeedbackModeling = new FeedbackModeling();
			   baselinefeedbackModeling.trecUser = trecUser;
			   InteractiveReranker baselineReranker = new Baseline(baselinefeedbackModeling);
			   baselineReranker.start(baselineResultSet, new double[]{0.5});
			   String[][] baselineAcc = new String[10][];
			   
			   ResultSet resultSet = null;
			   Feedback[] feedbacks = null;
		   
			   
			   for (int i = 0; i < 10; i++) {
				   
				    epsilon += 5;
				   
	    			double kl = trecUser.generateSubtopicsWithNoise(epsilon, baselineResultSet.docnos);
	    			resultSet = xQuADReranker.get();
	    			xQuADAcc[i] = resultSet.docnos;
	    			feedbacks = trecUser.get(resultSet);
	    			xQuADReranker.update(feedbacks);
	    			Evaluator.writeToFile(args[0] + "_xQuAD_" + k , topicId, resultSet, i);
	    			
	    			
	    			resultSet = PM2Reranker.get();
	    			PM2Acc[i] = resultSet.docnos;
	    			feedbacks = trecUser.get(resultSet);
	    			PM2Reranker.update(feedbacks);
	    			Evaluator.writeToFile(args[0] +"_PM2_" + k , topicId, resultSet, i);
	    			
	    			resultSet = xMMRReranker.get();
	    			xMMRAcc[i] = resultSet.docnos;
	    			feedbacks = trecUser.get(resultSet);
	    			xMMRReranker.update(feedbacks);
	    			Evaluator.writeToFile(args[0] +"_xMMR_" + k , topicId, resultSet, i);
	    			
	    			resultSet = baselineReranker.get();
	    			baselineAcc[i] = resultSet.docnos;
	    			feedbacks = trecUser.get(resultSet);
	    			baselineReranker.update(feedbacks);
	    			
	    			Evaluator.writeToFile(args[0] + "_" + k , topicId, resultSet, i);
	    			
	        		double actxQuAD = cubeTest.getAverageCubeTest(i+1, topicId, xQuADAcc);
	        		double actxPM2 = cubeTest.getAverageCubeTest(i+1, topicId, PM2Acc);
	        		double actxMMR = cubeTest.getAverageCubeTest(i+1, topicId, xMMRAcc);
	        		double actbaseline = cubeTest.getAverageCubeTest(i+1, topicId, baselineAcc);
	        		
	        		out.println(topicId + " " + k +  "  " + (i+1) + " " + kl  + " " + actxQuAD + " " +actxPM2 + " "  +actxMMR +" " +actbaseline  );
		    			
				}
			   trecUser.destroySubtopics();
		   }

    		
    		
    		count++;
    		if (count % 1000 == 0) {
    			System.out.println(count);
    		}
			
	    }
		br.close();
		out.close();

	}

}
