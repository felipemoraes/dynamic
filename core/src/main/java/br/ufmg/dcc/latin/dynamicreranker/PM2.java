package br.ufmg.dcc.latin.dynamicreranker;

import java.util.Arrays;

import br.ufmg.dcc.latin.aspectmodeling.PassageAspectModel;
import br.ufmg.dcc.latin.baselineranker.ResultList;
import br.ufmg.dcc.latin.utils.SharedCache;

public class PM2 extends DynamicReranker {
	
	double lambda;
	int[] highestAspect;
	
	double[] v;
	double[] s;
	double[][] coverage;
	
	
	protected int highestAspect(){
		
		int maxQ =  -1;
		double maxQuotient = -1;
		for (int i = 0; i < v.length; i++) {
			double quotient = v[i]/(2*s[i]+1);
			if (quotient > maxQuotient) {
				maxQ = i;
				maxQuotient = quotient;
			}
		}
		
		return maxQ;
	}
	
	public ResultList getResultList(PassageAspectModel aspectModel){
		updateCoverage(aspectModel);
		updateNovelty();
		return super.getResultList(aspectModel);
	}
	
	private void updateCoverage(PassageAspectModel aspectModel) {
		String[] aspects = aspectModel.getAspects();
		int aSize = aspects.length;
		int n = SharedCache.docnos.length;
		v = new double[aSize];
		s = new double[aSize];
		if (aSize > 0) {
			Arrays.fill(v, 1f/aSize);
		}
		Arrays.fill(s, 1);
		coverage = new double[n][aSize];
		for (int i = 0; i < aspects.length; i++) {
			double[] aspectCoverage = aspectModel.getAspectFlatCoverage(aspects[i]);
			for (int j = 0; j < aspectCoverage.length; j++) {
				coverage[j][i] = aspectCoverage[j];
			}
		}
	}

	@Override
	protected double score(int docid) {
		int q = highestAspect();
		
		if (q == -1){
			return relevance[docid];
		}
		
		double quotientAspectq = v[q]/(2*s[q]+1);
		quotientAspectq *= coverage[docid][q];
		double quotientotherAspect  = 0;
		for (int i = 0; i < s.length; i++) {
			if (i != q) {
				quotientotherAspect += (v[i]/(2*s[i]+1))*coverage[docid][i];
			}
		}
		double score = lambda*quotientAspectq + (1-lambda)*quotientotherAspect;
		
		return score;
	}

	@Override
	protected void update(int docid) {

		int q = highestAspect[docid];
		if (q == -1) {
			q = highestAspect();
		} 
		
		highestAspect[docid] = q;
		
		if (q == -1) {
			return;
		}
		
		double allCoverage = 0;
		
		for (int i = 0; i < coverage[docid].length; ++i) {
			allCoverage += coverage[docid][i];
		}
		
		if (allCoverage > 0) {
			s[q] += coverage[docid][q]/allCoverage;
		} 

	}
	
	public void updateNovelty(){
		for (int j = 0; j < docids.length; ++j) {
			if (!selected.has(j)) {
				continue;
			}
			update(j);
		}
	}

}
