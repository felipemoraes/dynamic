package br.ufmg.dcc.latin.utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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
    		for (int i = 0; i < estimated.length; i++) {
    			squaredDiff += Math.pow((truth[i][j]-estimated[i][j]),2);
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
	
	
}
