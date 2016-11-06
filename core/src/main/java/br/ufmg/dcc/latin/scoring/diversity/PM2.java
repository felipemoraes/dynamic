package br.ufmg.dcc.latin.scoring.diversity;

import br.ufmg.dcc.latin.diversity.Aspect;

public class PM2 {
	Aspect[] v;
	Aspect[] s;
	Aspect[][] coverage;
	float lambda;
	public PM2(Aspect[] v, Aspect[] s, Aspect[][] coverage, float lambda){
		this.v = v;
		this.s = s;
		this.coverage = coverage;
		this.lambda = lambda;
	}
	
	public int highestAspect(){
		int maxQ =  -1;
		float maxQuotient = -1;
		for (int i = 0; i < v.length; i++) {
			float quotient = v[i].getValue()/(2*s[i].getValue()+1);
			if (quotient > maxQuotient) {
				maxQ = i;
				maxQuotient = quotient;
			}
		}
		return maxQ;
	}
	
	public float score(int d, int q){
		
		float quotientAspectq = v[q].getValue()/(2*s[q].getValue()+1);
		quotientAspectq *= coverage[d][q].getValue();
		float quotientotherAspect  = 0;
		for (int i = 0; i < s.length; i++) {
			if (i != q) {
				quotientotherAspect += (v[i].getValue()/(2*s[i].getValue()+1))*coverage[d][i].getValue();
			}
		}
		return lambda*quotientAspectq + (1-lambda)*quotientotherAspect;
	}
	
	public void update(int d, int q){
		float allCoverage = 0;
		for (int i = 0; i < coverage[d].length; ++i) {
			allCoverage += coverage[d][i].getValue();
		}
		float newS = s[q].getValue() + coverage[d][q].getValue()/allCoverage;
		s[q].setValue(newS);
	}
}
