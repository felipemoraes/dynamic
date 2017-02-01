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
import br.ufmg.dcc.latin.stopping.FixedDepth;
import br.ufmg.dcc.latin.stopping.Stopping;
import br.ufmg.dcc.latin.user.FeedbackList;
import br.ufmg.dcc.latin.user.TrecDDUser;
import br.ufmg.dcc.latin.user.User;
import br.ufmg.dcc.latin.user.UserQuery;
import br.ufmg.dcc.latin.utils.CoverageError;
import br.ufmg.dcc.latin.utils.TopicsFile;

public class CoverageAnalysis {

	public static void main(String[] args) throws IOException {
		User user = new TrecDDUser();
		BaselineRanker baselineRanker = new AdHocBaselineRanker("DPH", new double[]{0.15,0.85});
		CoverageError coverageError = new CoverageError();
		
	    FileWriter fw = new FileWriter( "CoverageError.txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		
		for (UserQuery userQuery : TopicsFile.getTrecDD()) {
			//if (!userQuery.tid.equals("DD16-3")){
		    //	continue;
		    //}
			System.out.println(userQuery.tid);
			int iteration = 1;
			ResultList resultList = baselineRanker.getResultList(userQuery);
			FeedbackList feedbackList = user.getFeedbackSet(userQuery.tid, resultList);
			PassageAspectModeling aspectModeling = new PassageAspectModeling();
			xQuAD dynamicReranker = new xQuAD(0.5, 1000);
			Stopping stopping = new FixedDepth();
			while (!stopping.stop(feedbackList)) {
				iteration++;
				PassageAspectModel passageAspectModel = aspectModeling.getAspectModel(feedbackList);
				out.println(userQuery.tid + " " + iteration   + " " + coverageError.getRmse(userQuery.tid, passageAspectModel)
						+ " " + coverageError.getSpearman(userQuery.tid, passageAspectModel)
						+ " " + coverageError.getKendall(userQuery.tid, passageAspectModel)
						);
				resultList = dynamicReranker.getResultList(passageAspectModel);
				feedbackList = user.getFeedbackSet(userQuery.tid, resultList);
				
			}
		}
		out.close();
	}

}
