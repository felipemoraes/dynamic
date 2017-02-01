package br.ufmg.dcc.latin.dynamicreranker;


import java.util.Arrays;

import org.apache.commons.math3.stat.StatUtils;

import br.ufmg.dcc.latin.aspectmodeling.PassageAspectModel;
import br.ufmg.dcc.latin.baselineranker.ResultList;
import br.ufmg.dcc.latin.utils.SharedCache;

public class xQuAD extends DynamicReranker {

	double[] importance;
	double[] novelty;
	double[][] coverage;
	
	double lambda;
	
	public xQuAD(double lambda, int depth){
		this.lambda = lambda;
		this.depth = depth;
		this.docids = SharedCache.docids;
		this.docnos = SharedCache.docnos;
		this.relevance = normalize(SharedCache.scores);
		selected = new BooleanSelectedSet(this.docnos.length);
	}

	public ResultList getResultList(PassageAspectModel aspectModel){
		updateCoverage(aspectModel);
		updateNovelty();
		return super.getResultList(aspectModel);
	}
	
	public double[] normalize(double[] values) {
		double[] newValues = new double[values.length];
		double sum = StatUtils.sum(values);
		for (int i = 0; i < newValues.length; i++) {
			newValues[i] = values[i]/sum;
		}
		return newValues;
	}
	
	private void updateCoverage(PassageAspectModel aspectModel) {
		String[] aspects = aspectModel.getAspects();
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;
		novelty = new double[aSize];
		importance = new double[aSize];
		if (aSize > 0) {
			Arrays.fill(importance, 1f/aSize);
		}
		Arrays.fill(novelty, 1);
		coverage = new double[n][aSize];
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
		normalizeCoverage();
	}

	@Override
	public double score(int docid){
		if (importance.length == 0) {
			return relevance[docid];
		}
		
		float diversity = 0;
		for (int i = 0; i < importance.length; i++) {
			diversity +=  importance[i]*coverage[docid][i]*novelty[i];
		}
		double score = (1-lambda)*relevance[docid] + lambda*diversity;
		
		return score;
	}
	
	@Override
	public void update(int docid){
		for (int i = 0; i < novelty.length; i++) {
			novelty[i] *= (1-coverage[docid][i]);
		}
		
	}
	
	public void updateNovelty(){
		for (int j = 0; j < docids.length; ++j) {
			if (! selected.has(j)) {
				continue;
			}
			update(j);
		}
	}

	protected void normalizeCoverage(){
		if (coverage.length == 0) {
			return;
		}
		for (int i = 0; i < coverage[0].length; ++i) {
			float sum = 0;
			for (int j = 0; j < coverage.length; j++) {
				sum += coverage[j][i];
			}
			
			for (int j = 0; j < coverage.length; j++) {
				if (sum > 0) {
					double normValue = coverage[j][i]/sum;
					coverage[j][i] = normValue;
				}
				
			}
		}
	}
}
