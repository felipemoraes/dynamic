package br.ufmg.dcc.latin.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.apache.commons.math3.stat.StatUtils;
import org.apache.commons.math3.stat.correlation.KendallsCorrelation;
import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;

import br.ufmg.dcc.latin.aspectmodeling.PassageAspectModel;
import br.ufmg.dcc.latin.user.Passage;
import br.ufmg.dcc.latin.user.RelevanceSet;

public class CoverageError {
	private Map<String, RelevanceSet> repository;
	
	public CoverageError(){
		repository = new HashMap<String,RelevanceSet>();
		try (BufferedReader br = new BufferedReader(new FileReader("../share/truth_data.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(",",5);
		    	Passage passage = new Passage(splitLine[2],Integer.parseInt(splitLine[4]),Integer.parseInt(splitLine[3]));
		    	if (passage.relevance == 0 ) {
		    		passage.relevance = 1;
		    	}
		    	if (!repository.containsKey(splitLine[0])){
		    		repository.put(splitLine[0], new RelevanceSet());
		    	} 
		    	
		    	repository.get(splitLine[0]).add(splitLine[1], passage);
			}
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	
	
	public double getRmse(String topicId, PassageAspectModel aspectModel){
		String[] aspects = aspectModel.getAspects();
		
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;

		double[][] coverage = new double[n][aSize];
		
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
		
		double[][] realCoverage = getRealCoverage(topicId,aspects);
		
		return rmse(realCoverage, coverage);
	}
	
	public double getSpearman(String topicId, PassageAspectModel aspectModel){
		String[] aspects = aspectModel.getAspects();
		
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;

		double[][] coverage = new double[n][aSize];
		
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
		
		double[][] realCoverage = getRealCoverage(topicId,aspects);
		
		return spearman(realCoverage, coverage);
	}
	
	public double getDivergence(String topicId, PassageAspectModel aspectModel){
		String[] aspects = aspectModel.getAspects();
		
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;

		double[][] coverage = new double[n][aSize];
		
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
		
		double[][] realCoverage = getRealCoverage(topicId,aspects);
		
		return divergence(realCoverage, coverage);
	}
	

    private double klDivergence(double[] p1, double[] p2) {
    double log2 = Math.log(2);

      double klDiv = 0.0;

      for (int i = 0; i < p1.length; ++i) {
        if (p1[i] == 0) { continue; }
        if (p2[i] == 0.0) { continue; } // Limin

      klDiv += p1[i] * Math.log( p1[i] / p2[i] );
      }

      return klDiv / log2; // moved this division out of the loop -DM
    }

	private double divergence(double[][] truth, double[][] estimated) {
    	if (estimated.length == 0) {
    		return 0;
    	}
    	double error = 0;
    	for (int j = 0; j < estimated[0].length; j++) {
    		double[] v1 = new double[estimated.length];
    		double[] v2 = new double[estimated.length];
    		for (int i = 0; i < estimated.length; i++) {
    			v1[i] = truth[i][j];
    			v2[i] = estimated[i][j];
    		}
    		double sum1 = StatUtils.sum(v1);
    		double sum2 = StatUtils.sum(v2);
    		for (int i = 0; i < estimated.length; i++) {
    			v1[i] = v1[i]/sum1;
    			v2[i] = v2[i]/sum2;
    		}
    		error += klDivergence(v1, v2);
		}
    
		if (estimated[0].length == 0) {
			return 0;
		}
		return error/estimated[0].length;
		
	}



	public double getKendall(String topicId, PassageAspectModel aspectModel){
		String[] aspects = aspectModel.getAspects();
		
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;

		double[][] coverage = new double[n][aSize];
		
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
		
		double[][] realCoverage = getRealCoverage(topicId,aspects);
		
		return kendall(realCoverage, coverage);
	}

	private double[][] getRealCoverage(String topicId, String[] aspects) {
		
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;
		
		double[][] coverage = new double[n][aSize];
		
		String[] docnos = SharedCache.docnos;
		
		for (int i = 0; i < docnos.length; i++) {
			if (repository.containsKey(docnos[i])) {
				
				for (int j = 0; j < aspects.length; j++) {
					coverage[i][j] = repository.get(docnos[i]).getRelevance(topicId,aspects[j]);
				}
				
			} else {
				for (int j = 0; j < aspects.length; j++) {
					coverage[i][j] = 0;
				}
			}
		}
		

		
		return coverage;

	}
	
	
	
    private double rmse(double[][] truth, double[][] estimated) {
    	if (estimated.length == 0) {
    		return 0;
    	}
    	double error = 0;
    	for (int j = 0; j < estimated[0].length; j++) {
    		double squaredDiff = 0;
    		double[] v1 = new double[estimated.length];
    		double[] v2 = new double[estimated.length];
    		for (int i = 0; i < estimated.length; i++) {
    			v1[i] = truth[i][j];
    			v2[i] = estimated[i][j];
    		}
    		double max1 = StatUtils.max(v1);
    		double max2 = StatUtils.max(v2);
    		
    		double min1 = StatUtils.min(v1);
    		double min2 = StatUtils.min(v2);
    		
    		for (int i = 0; i < estimated.length; i++) {
    			double value1 = (truth[i][j]-min1)/(max1-min1);
    			double value2 = (estimated[i][j]-min2)/(max2-min2);
    			squaredDiff += Math.pow((value1-value2),2);
    		}
    		
    		squaredDiff = squaredDiff/truth.length;
    		error += squaredDiff;
		}
    
		if (estimated[0].length == 0) {
			return 0;
		}
		return error/estimated[0].length;
	}
    
    private double spearman(double[][] truth, double[][] estimated) {
    	if (estimated.length == 0) {
    		return 0;
    	}
    	SpearmansCorrelation sp = new SpearmansCorrelation();
    	double error = 0;
    	for (int j = 0; j < estimated[0].length; j++) {
    		double[] v1 = new double[estimated.length];
    		double[] v2 = new double[estimated.length];
    		for (int i = 0; i < estimated.length; i++) {
    			v1[i] = truth[i][j];
    			v2[i] = estimated[i][j];
    		}
    		error += sp.correlation(v1, v2);
		}
    
		if (estimated[0].length == 0) {
			return 0;
		}
		return error/estimated[0].length;
	}
    
    private double kendall(double[][] truth, double[][] estimated) {
    	if (estimated.length == 0) {
    		return 0;
    	}
    	KendallsCorrelation kc = new KendallsCorrelation();
    	double error = 0;
    	for (int j = 0; j < estimated[0].length; j++) {
    		double[] v1 = new double[estimated.length];
    		double[] v2 = new double[estimated.length];
    		for (int i = 0; i < estimated.length; i++) {
    			v1[i] = truth[i][j];
    			v2[i] = estimated[i][j];
    		}
    		error += kc.correlation(v1, v2);
		}
    
		if (estimated[0].length == 0) {
			return 0;
		}
		return error/estimated[0].length;
	}



	public double getTauAP(String tid, PassageAspectModel aspectModel) {
		String[] aspects = aspectModel.getAspects();
		
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;

		double[][] coverage = new double[n][aSize];
		
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
		
		double[][] realCoverage = getRealCoverage(tid,aspects);
		
		return tauAPIA(realCoverage, coverage);
	}
	
	public double getNdcg(String tid, PassageAspectModel aspectModel) {
		String[] aspects = aspectModel.getAspects();
		
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;

		double[][] coverage = new double[n][aSize];
		
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
		
		double[][] realCoverage = getRealCoverage(tid,aspects);
		
		return ndcgIA(realCoverage, coverage);
	}
	
    private double tauAPIA(double[][] truth, double[][] estimated) {
    	
    	if (estimated.length == 0) {
    		return 0;
    	}
    	TauApCorrelation tauAP = new TauApCorrelation();
    	double error = 0;
    	for (int j = 0; j < estimated[0].length; j++) {
    		double[] v1 = new double[estimated.length];
    		double[] v2 = new double[estimated.length];
    		for (int i = 0; i < estimated.length; i++) {
    			v1[i] = truth[i][j];
    			v2[i] = estimated[i][j];
    		}
    		error += tauAP.correlation(v1, v2);
		}
    
		if (estimated[0].length == 0) {
			return 0;
		}
		return error/estimated[0].length;
	}
    
    private double ndcgIA(double[][] truth, double[][] estimated) {
    	
    	if (estimated.length == 0) {
    		return 0;
    	}
    	
    	
    	double error = 0;
    	for (int j = 0; j < estimated[0].length; j++) {
    		double[] v1 = new double[estimated.length];
    		double[] v2 = new double[estimated.length];
    		for (int i = 0; i < estimated.length; i++) {
    			v1[i] = truth[i][j];
    			v2[i] = estimated[i][j];
    		}
    		error += ndcg(v1, v2);
		}
    
		if (estimated[0].length == 0) {
			return 0;
		}
		return error/estimated[0].length;
	}



	private double ndcg(double[] v1, double[] v2) {
		
		int[] indices = IntStream.range(0, v2.length)
                .boxed().sorted((i, j) -> (new Double(v2[i])).compareTo(v2[j])*-1 )
                .mapToInt(ele -> ele).toArray();
		double dcg = 0;
		for (int k = 1; k <= indices.length; k++) {
			dcg += (Math.pow(2, v1[indices[k-1]])-1)/Math.log(k+1);
		}
		indices = IntStream.range(0, v1.length)
                .boxed().sorted((i, j) -> (new Double(v1[i])).compareTo(v1[j])*-1 )
                .mapToInt(ele -> ele).toArray();
		double idcg = 0;
		for (int k = 1; k <= indices.length; k++) {
			idcg += (Math.pow(2, v1[k-1])-1)/Math.log(k+1);
		}
		// TODO Auto-generated method stub
		if (dcg == 0) {
			return 0;
		}
		
		if (idcg == 0){
			return 0;
		}
		return dcg/idcg;
	}
	
}
