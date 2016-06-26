/**
 * 
 */
package br.ufmg.dcc.latin.features;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.lucene.search.similarities.AfterEffect;
import org.apache.lucene.search.similarities.AfterEffectB;
import org.apache.lucene.search.similarities.AfterEffectL;
import org.apache.lucene.search.similarities.BasicModel;
import org.apache.lucene.search.similarities.BasicModelBE;
import org.apache.lucene.search.similarities.BasicModelD;
import org.apache.lucene.search.similarities.BasicModelG;
import org.apache.lucene.search.similarities.BasicModelIF;
import org.apache.lucene.search.similarities.BasicModelIn;
import org.apache.lucene.search.similarities.BasicModelIne;
import org.apache.lucene.search.similarities.BasicModelP;
import org.apache.lucene.search.similarities.Distribution;
import org.apache.lucene.search.similarities.DistributionLL;
import org.apache.lucene.search.similarities.DistributionSPL;
import org.apache.lucene.search.similarities.Independence;
import org.apache.lucene.search.similarities.IndependenceChiSquared;
import org.apache.lucene.search.similarities.IndependenceSaturated;
import org.apache.lucene.search.similarities.IndependenceStandardized;
import org.apache.lucene.search.similarities.Lambda;
import org.apache.lucene.search.similarities.LambdaDF;
import org.apache.lucene.search.similarities.LambdaTTF;
import org.apache.lucene.search.similarities.Normalization;
import org.apache.lucene.search.similarities.NormalizationH1;
import org.apache.lucene.search.similarities.NormalizationH2;
import org.apache.lucene.search.similarities.NormalizationH3;
import org.apache.lucene.search.similarities.NormalizationZ;

import br.ufmg.dcc.latin.searcher.matching.BM25Scorer;
import br.ufmg.dcc.latin.searcher.matching.DFIScorer;
import br.ufmg.dcc.latin.searcher.matching.DFRScorer;
import br.ufmg.dcc.latin.searcher.matching.IBScorer;
import br.ufmg.dcc.latin.searcher.matching.LMDirichletScorer;
import br.ufmg.dcc.latin.searcher.matching.LMJelinekMercerScorer;
import br.ufmg.dcc.latin.searcher.matching.Scorer;
import br.ufmg.dcc.latin.searcher.matching.TFIDFScorer;

/**
 * @author Felipe Moraes
 *
 */
public class FeaturesService {
	
	
	private Map<String,Scorer> scorers;
	
	public Map<String,Scorer> getScorers(){
		return scorers;
	}
	
	public float[] getQueryIndependentFeatures(int docId){
		float[] features = new float[0];
		return features;
	}
	
	public FeaturesService() {
		
		this.scorers = new HashMap<String,Scorer>();
		
		
		
		HashMap<String,Independence> independences = new HashMap<String,Independence>();
		independences.put("chisquared", new IndependenceChiSquared());
		independences.put("saturated", new IndependenceSaturated());
		independences.put("standardized", new IndependenceStandardized());
		
		HashMap<String,Distribution> distributions = new HashMap<String,Distribution>();
		distributions.put("ll", new DistributionLL());
		distributions.put("sll", new DistributionSPL());
		
		HashMap<String, Lambda> lambdas = new HashMap<String, Lambda>();
		lambdas.put("df", new LambdaDF());
		lambdas.put("ttf", new LambdaTTF());
		
		HashMap<String, BasicModel> basicModels = new HashMap<String, BasicModel>();
		basicModels.put("be", new BasicModelBE());
		basicModels.put("d", new BasicModelD());
		basicModels.put("g", new BasicModelG());
		basicModels.put("if", new BasicModelIF());
		basicModels.put("in", new BasicModelIn());
		basicModels.put("ine", new BasicModelIne());
		basicModels.put("p", new BasicModelP());
		
		HashMap<String, AfterEffect> afterEffects = new HashMap<String, AfterEffect>();
		
		afterEffects.put("no", new AfterEffect.NoAfterEffect() );
		afterEffects.put("b", new  AfterEffectB());
		afterEffects.put("l", new  AfterEffectL());
		
		HashMap<String, Normalization> normalizations = new HashMap<String, Normalization>();
		normalizations.put("no", new Normalization.NoNormalization());
		normalizations.put("h1", new NormalizationH1());
		normalizations.put("h2", new NormalizationH2());
		normalizations.put("h3", new NormalizationH3());
		normalizations.put("z", new NormalizationZ());
	
		
		scorers.put("BM25", new BM25Scorer());
		scorers.put("TFIDF", new TFIDFScorer());
		scorers.put("LMDirichlet", new LMDirichletScorer(2500));
		scorers.put("LMJelinekMercer", new LMJelinekMercerScorer( 0.1F));
		for (Entry<String,Independence> independence : independences.entrySet()) {
			scorers.put("DFI_" + independence.getKey(), new DFIScorer(independence.getValue()));
		}		
		for (Entry<String,BasicModel> basicModel : basicModels.entrySet()) {
			for (Entry<String,AfterEffect> afterEffect : afterEffects.entrySet()) {
				for (Entry<String,Normalization> normalization: normalizations.entrySet()) {
					scorers.put("DFR_" + basicModel.getKey() + "_" 
							+ afterEffect.getKey() + "_" + normalization.getKey(),
							new DFRScorer(basicModel.getValue(), afterEffect.getValue(), normalization.getValue()));
				}
			}
		}
		
		
		for (Entry<String,Lambda> lambda : lambdas.entrySet()) {
			for (Entry<String,Distribution> distribution: distributions.entrySet()) {
				for (Entry<String,Normalization> normalization: normalizations.entrySet()) {
					scorers.put("IB_" + distribution.getKey() + "_" + lambda.getKey() + "_" 
				    + normalization.getKey(), new IBScorer(distribution.getValue(), lambda.getValue(),
				    		normalization.getValue()));
				}
			}
		}
	}
	
}
