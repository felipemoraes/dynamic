package br.ufmg.dcc.latin.experiment;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

public class SimulatedRelevance {
	
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
	
	public static List<TargetAP> loadTargetAPs() throws IOException{
		List<TargetAP> targetAPs = new ArrayList<TargetAP>();
		BufferedReader br = new BufferedReader(new FileReader("TargetAPs.txt"));
	    String line;
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",2);
	    	TargetAP tAP = new TargetAP();
			tAP.bin = Double.parseDouble(splitLine[0]);
			tAP.AP = Double.parseDouble(splitLine[1]);
			targetAPs.add(tAP);	
	    }
	    br.close();
		return targetAPs;
	}
	
	public static void main(String[] args) throws IOException {
		
		CubeTest cubeTest = new CubeTest();
		
		String topicsFile = "../share/topics_domain.txt";
	
		
		BufferedReader br = new BufferedReader(new FileReader(topicsFile));
	    String line;

	    BaselineRanker baselineRanker = getBaselineRanker("DPH");
	    TrecUser trecUser = TrecUser.getInstance("../share/truth_data.txt");
	   
	    SimAP.trecUser = trecUser;
	    List<TargetAP> targetAPs = loadTargetAPs();
	    /*List<TargetAP> targetAPs = new ArrayList<TargetAP>();
	    for (int i = 0; i < 19 ; i++) {
	    	for (int j = 0; j <20; j++) {
				TargetAP tAP = new TargetAP();
				tAP.bin = i*(0.05);
				tAP.AP = ThreadLocalRandom.current().nextDouble(i*(0.05), (i+1)*(0.05));
				targetAPs.add(tAP);	
			}
		}
	    for (int j = 0; j <20; j++) {
		    TargetAP tAP = new TargetAP();
			tAP.bin = 0.95;
			tAP.AP = ThreadLocalRandom.current().nextDouble(0.95, 1.00000000000000000000000001);
			targetAPs.add(tAP);
		}*/
	    
	    
	    FileWriter fw = new FileWriter("SimulatedRelevance_DPH_500.txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
	    		
	    while ((line = br.readLine()) != null) {
	    	String[] splitLine = line.split(" ",3);
	    	
        	String topicId = splitLine[1];
        	//if (!topicId.equals("DD16-1")){
        	//	continue;
        	//}
        	
        	System.out.println(topicId);
        	TrecUser.topicId = topicId;
    		String query = splitLine[2].replaceAll("/", " ");
    		String index = splitLine[0];
    		ResultSet baselineResultSet = baselineRanker.search(query, index);
    		trecUser.generateSubtopics(baselineResultSet.docnos);
    		int count = 0;
    		for (TargetAP targetAP : targetAPs) {
    			
   				SimAP.targetAP = targetAP.AP;
   	
				baselineResultSet = baselineRanker.search();
				
			    FeedbackModeling feedbackModeling = new FeedbackModeling();
			    feedbackModeling.trecUser = trecUser;
			    InteractiveReranker reranker = new xQuAD(feedbackModeling);
				reranker.start(baselineResultSet, new double[]{0.5});
    			String[][] accResultxQuAD = new String[10][];
        		for (int i = 0; i < 10; i++) {
        			ResultSet resultSet = reranker.get();
        			accResultxQuAD[i] = resultSet.docnos;
        			Feedback[] feedbacks = trecUser.get(resultSet);
        			reranker.update(feedbacks);
    			}
        		
			    feedbackModeling = new FeedbackModeling();
			    feedbackModeling.trecUser = trecUser;
			    reranker = new PM2(feedbackModeling);
				reranker.start(baselineResultSet, new double[]{0.5});
    			String[][] accResultPM2 = new String[10][];
    			
        		for (int i = 0; i < 10; i++) {
        			ResultSet resultSet = reranker.get();
        			accResultPM2[i] = resultSet.docnos;
        			Feedback[] feedbacks = trecUser.get(resultSet);
        			reranker.update(feedbacks);
    			}
        		
        		
			    feedbackModeling = new FeedbackModeling();
			    feedbackModeling.trecUser = trecUser;
			    reranker = new Baseline(feedbackModeling);
				reranker.start(baselineResultSet, new double[]{0.5});
    			String[][] accBaseLineResult = new String[10][];
        		for (int i = 0; i < 10; i++) {
        			ResultSet resultSet = reranker.get();
        			accBaseLineResult[i] = resultSet.docnos;
        			Feedback[] feedbacks = trecUser.get(resultSet);
        			reranker.update(feedbacks);
    			}
        		
 
    			double precisionbaseline = cubeTest.getPrecision(1, topicId, accBaseLineResult);
    			

    			double recallbaseline = cubeTest.getRecall(500, topicId, baselineResultSet.scores, baselineResultSet.docnos);
        		for (int i = 0; i < 10; i++) {
        			double actxQuAD = cubeTest.getAverageCubeTest(i+1, topicId, accResultxQuAD);
        			double actPM2 = cubeTest.getAverageCubeTest(i+1, topicId, accResultPM2);
        			double actbaseline = cubeTest.getAverageCubeTest(i+1, topicId, accBaseLineResult);


        			out.println(topicId + " " + (i+1) + " " + targetAP.bin  + " " + SimAP.targetAP + " " + SimAP.currentAP + " " + actxQuAD + " " + actPM2 + " " +actbaseline 
        					+ " " +precisionbaseline
        					+ " " +recallbaseline);
				}
        		
        		count++;
        		if (count % 100 == 0) {
        			System.out.println(count);
        		}
        		
    		}	
	    }
		br.close();
		out.close();
	}
	
	static class TargetAP {
		double bin;
		double AP;
	}

}
