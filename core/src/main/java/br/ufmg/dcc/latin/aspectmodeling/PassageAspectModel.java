package br.ufmg.dcc.latin.aspectmodeling;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.stat.StatUtils;

import br.ufmg.dcc.latin.utils.RescorerSystem;
import br.ufmg.dcc.latin.utils.RetrievalSystem;
import br.ufmg.dcc.latin.utils.SharedCache;
import gnu.trove.list.array.TDoubleArrayList;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;


public class PassageAspectModel extends AspectModel implements DiversityAspectModel {
	Map<String,PassageAspect> model;
	TIntObjectMap<AspectNode> docs;
	
	public PassageAspectModel(){
		model = new HashMap<String,PassageAspect>();
		docs = new TIntObjectHashMap<AspectNode>();
	}
	
	public void addToAspect(String aspectId, int passageId, double relevance){
		if (!model.containsKey(aspectId)){
			model.put(aspectId, new PassageAspect());
		}
		model.get(aspectId).addPassage(passageId, relevance);
	}

	public void addToDocument(int docid, String aspectId, double relevance) {
		if (!docs.containsKey(docid)){
			docs.put(docid, new AspectNode());
		}
		docs.get(docid).addNode(aspectId, relevance);
	}
	
	public double[] getAspectFlatCoverage(String aspectId){
		int[] docids = SharedCache.docids;
		int n = docids.length;
		double[] relevance = new double[n];
		PassageAspect passageAspect =  model.get(aspectId);
		for (int passageId : passageAspect.getPassages()) {
			String passage = RetrievalSystem.getPassage(passageId);
			double[] scores = RescorerSystem.rescore(passage);
			scores = scale(scores);
			scores = scalarMultiply(scores, passageAspect.getPassageRelevance(passageId));
			for (int i = 0; i < scores.length; i++) {
				if (relevance[i] < scores[i]){
					relevance[i] = scores[i];
				}
			}
		}
	
		for (int i = 0; i < docids.length; i++) {
			if (docs.containsKey(docids[i])) {
				relevance[i] = docs.get(docids[i]).getRelevance(aspectId);
			}
		}
		
		return relevance;
	}
	
	
	public double[] scalarMultiply(double[] values, double value){
		double[] result = new double[values.length];

		for (int i = 0; i < values.length; i++) {
			result[i] = values[i]*value;
		}
		return result;
	}
	
	
	public double[] scale(double[] values){
		double[] result = new double[values.length];
		double max = StatUtils.max(values);
		double min = StatUtils.min(values);
		if (max==min) {
			return result;
		}
		for (int i = 0; i < values.length; i++) {
			result[i] = (values[i]-min)/(max-min); 
		}
		return result;
	}
	
	public double[][] getAspectHierchicalCoverage(String aspectId){
		return null;
	}
	
	class AspectNode {
		Map<String,TDoubleArrayList> nodes;
		public AspectNode () {
			nodes = new HashMap<String,TDoubleArrayList>();
		}
		
		public void addNode(String aspectId, double relevance){
			if (!nodes.containsKey(aspectId)) {
				nodes.put(aspectId, new TDoubleArrayList());
			}
			nodes.get(aspectId).add(relevance);
		}
		
		public double getRelevance(String aspectId){
			if (!nodes.containsKey(aspectId)) {
				return 0;
			}
			return StatUtils.max(nodes.get(aspectId).toArray());
		}
	}

	public String[] getAspects() {
		int aSize = model.size();
		String[] aspects = new String[aSize];
		int i = 0;
		for (String aspect : model.keySet()) {
			aspects[i] = aspect;
			i++;
		}
		return aspects;
	}
	
}
