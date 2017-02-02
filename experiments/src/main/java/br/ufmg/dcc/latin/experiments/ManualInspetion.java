package br.ufmg.dcc.latin.experiments;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import br.ufmg.dcc.latin.aspectmodeling.PassageAspectModel;
import br.ufmg.dcc.latin.aspectmodeling.PassageAspectModeling;
import br.ufmg.dcc.latin.baselineranker.AdHocBaselineRanker;
import br.ufmg.dcc.latin.baselineranker.BaselineRanker;
import br.ufmg.dcc.latin.baselineranker.ResultList;
import br.ufmg.dcc.latin.dynamicreranker.xQuAD;
import br.ufmg.dcc.latin.metrics.CubeTest;
import br.ufmg.dcc.latin.stopping.FixedDepth;
import br.ufmg.dcc.latin.stopping.Stopping;
import br.ufmg.dcc.latin.user.FeedbackList;
import br.ufmg.dcc.latin.user.TrecDDUser;
import br.ufmg.dcc.latin.user.User;
import br.ufmg.dcc.latin.user.UserQuery;
import br.ufmg.dcc.latin.utils.CoverageError;
import br.ufmg.dcc.latin.utils.SharedCache;
import br.ufmg.dcc.latin.utils.TopicsFile;

public class ManualInspetion {

	public static void main(String[] args) throws IOException {
		User user = new TrecDDUser();
		CubeTest cubeTest = new CubeTest();
		BaselineRanker baselineRanker = new AdHocBaselineRanker("DPH", new double[]{0.15,0.85});
		CoverageError coverageError = new CoverageError();
		
	    FileWriter fw = new FileWriter( "ebola16_stats.txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		
		for (UserQuery userQuery : TopicsFile.getTrecDD()) {
			if (!userQuery.index.equals("ebola16")){
		    	continue;
		    }
			String[][] xQuADDocs = new String[10][5];
			String[][] baseDocs = new String[10][5];
			//System.out.println(userQuery.tid);
			int iteration = 1;
			
			ResultList resultListTime1 = baselineRanker.getResultList(userQuery);
			xQuADDocs[iteration-1] = resultListTime1.docnos;
			FeedbackList feedbackList = user.getFeedbackSet(userQuery.tid, resultListTime1);
			PassageAspectModeling aspectModeling = new PassageAspectModeling();
			xQuAD dynamicReranker = new xQuAD(0.5, 1000);
			Stopping stopping = new FixedDepth();
			double aspectSize = 0;
			double spearman = 0;
			double divergence = 0;
			while (!stopping.stop(feedbackList)) {
				iteration++;
				PassageAspectModel passageAspectModel = aspectModeling.getAspectModel(feedbackList);
				aspectSize = (double) passageAspectModel.getAspects().length;
				spearman = coverageError.getSpearman(userQuery.tid, passageAspectModel);
				divergence = coverageError.getDivergence(userQuery.tid, passageAspectModel);
				ResultList resultListN = dynamicReranker.getResultList(passageAspectModel);
				xQuADDocs[iteration-1] = resultListN.docnos;
				feedbackList = user.getFeedbackSet(userQuery.tid, resultListN);
				
			}
			iteration = 1;
			
			baseDocs[iteration-1] = resultListTime1.docnos;
			feedbackList = user.getFeedbackSet(userQuery.tid, resultListTime1);
			aspectModeling = new PassageAspectModeling();
			xQuAD baseline = new xQuAD(0.0, 1000);
			stopping = new FixedDepth();
			while (!stopping.stop(feedbackList)) {
				iteration++;
				PassageAspectModel passageAspectModel = aspectModeling.getAspectModel(feedbackList);
				ResultList resultListN = baseline.getResultList(passageAspectModel);
				baseDocs[iteration-1] = resultListN.docnos;
				feedbackList = user.getFeedbackSet(userQuery.tid, resultListN);
				
			}
			out.println(userQuery.tid  
					+	" "	+ cubeTest.getAverageCubeTest(10, userQuery.tid , xQuADDocs) + " " + cubeTest.getAverageCubeTest(10, userQuery.tid , baseDocs)
					+	" "	+ cubeTest.getSRecall(10, userQuery.tid , xQuADDocs) + " " + cubeTest.getSRecall(10, userQuery.tid , baseDocs)
					+	" "	+ cubeTest.getRecall(userQuery.tid , SharedCache.docnos) 
					+	" "	+ cubeTest.getPrecision(userQuery.tid , SharedCache.docnos)  
					+	" " + spearman + " " + divergence+  " "  + aspectSize +  " " + cubeTest.getRelevants(userQuery.tid)
					);

		}
		
		out.close();
	}

}
