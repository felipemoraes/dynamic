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
import br.ufmg.dcc.latin.utils.QueryIndependentFeatures;
import br.ufmg.dcc.latin.utils.RetrievalSystem;
import br.ufmg.dcc.latin.utils.SharedCache;
import br.ufmg.dcc.latin.utils.TopicsFile;

public class CoverageAnalysis {

	public static void main(String[] args) throws IOException {
		User user = new TrecDDUser();
		BaselineRanker baselineRanker = new AdHocBaselineRanker("DPH", new double[]{0.15,0.85});
		CoverageError coverageError = new CoverageError();
		
	    FileWriter fw = new FileWriter( "CoverageError.txt");
	    BufferedWriter bw = new BufferedWriter(fw);
		PrintWriter out = new PrintWriter(bw);
		
		FileWriter fwf = new FileWriter( "QueryIndependentFeaturesStats.txt");
	    BufferedWriter bwf = new BufferedWriter(fwf);
		PrintWriter outf = new PrintWriter(bwf);
		
		
		String currentIndex = "-1";
		QueryIndependentFeatures qif = null;
		for (UserQuery userQuery : TopicsFile.getTrecDD()) {
			//if (!userQuery.tid.equals("DD16-1")){
		    //	continue;
		    //}
			
			//if (!userQuery.index.equals("ebola16")){
		    //	continue;
		    //}
			
			System.out.println(userQuery.tid);
			int iteration = 1;

			ResultList resultList = baselineRanker.getResultList(userQuery);
			
			if (!userQuery.index.equals(currentIndex)){
				currentIndex = userQuery.index;
				qif = new QueryIndependentFeatures(userQuery.index, RetrievalSystem.getIndexSize());
			}

			double[][] independentFeaturesStats = qif.getStatistics(SharedCache.docids);
			for (int i = 0; i < independentFeaturesStats.length; i++) {
				for (int j = 0; j < independentFeaturesStats[i].length; j++) {
					outf.print(independentFeaturesStats[i][j] + " ");
				}
			}
			outf.print("\n");
			
			FeedbackList feedbackList = user.getFeedbackSet(userQuery.tid, resultList);
			PassageAspectModeling aspectModeling = new PassageAspectModeling();
			xQuAD dynamicReranker = new xQuAD(1.0, 1000);
			Stopping stopping = new FixedDepth();
			while (!stopping.stop(feedbackList)) {
				iteration++;
				PassageAspectModel passageAspectModel = aspectModeling.getAspectModel(feedbackList);
				out.println(userQuery.tid + " " + iteration   + " " + coverageError.getRmse(userQuery.tid, passageAspectModel)
						+ " " + coverageError.getSpearman(userQuery.tid, passageAspectModel)
						+ " " + coverageError.getKendall(userQuery.tid, passageAspectModel)
						+ " " + coverageError.getTauAP(userQuery.tid, passageAspectModel)
						+ " " + coverageError.getNdcg(userQuery.tid, passageAspectModel)
 						);
				resultList = dynamicReranker.getResultList(passageAspectModel);
				feedbackList = user.getFeedbackSet(userQuery.tid, resultList);
				
			}
		}
		out.close();
		outf.close();
	}

}
