package br.ufmg.dcc.latin.simulation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import br.ufmg.dcc.latin.user.TrecUser;

public class SimAP {
	
	public static TrecUser trecUser;
	
	public static double targetAP;
	
	public static double currentAP;
	
	private static List<Integer> zeros;
	
	public static double[] getBins(String[] docnos) {
		double[] relevances = trecUser.get(docnos);
		int countRels = 0;
		for (int i = 0; i < relevances.length; i++) {
			if (relevances[i]>0) {
				countRels++;
			}
		}
		
		double[] bestRels = new double[relevances.length];
		for (int i = 0; i < countRels; i++) {
			bestRels[i] = 1;
		}
		
		double bestAP = computeAP(bestRels);
		Arrays.fill(bestRels, 1);
		for (int i = 0; i < bestRels.length-countRels; i++) {
			bestRels[i] = 0;
		}
		double worstAP = computeAP(bestRels);
		int i = 0;
		double firstBin = worstAP - (worstAP%0.05);
		double lastBin = bestAP - (bestAP % 0.05) + 0.05;
		int n = (int) ((lastBin - firstBin)/0.05) + 1;
		double[] bins = new double[n];
		while (firstBin <= lastBin) {
			bins[i] = firstBin;
			firstBin += 0.05;
			i++;
		}
		return bins;
		
	}

	private static void makeLists(double[] relevances){
		
		zeros = new ArrayList<Integer>();

		for (int i = 0; i < relevances.length; i++) {
			if (relevances[i] == 0) {
				zeros.add(i);
			}
		}
	}
	
	private static Pair getPairL(double[] relevances){
		
		Random rand = new Random();
		int listSize = zeros.size();
		while (true) {
			if (listSize == 0) {
				return null;
			}
			Pair pair = new Pair();
			int ix = rand.nextInt(listSize);
			List<Integer> ones = new ArrayList<Integer>();
			for (int i = ix+1; i < relevances.length; i++) {
				if (relevances[i] > 0) {
					ones.add(i);
				}
			}
			
			if (ones.size() > 0) {
				int j = rand.nextInt(ones.size());
				pair.ri = zeros.get(ix);
				pair.rj = ones.get(j);
				pair.i = ix;
				pair.j = j;
				
				return pair;
			}
		}
	}
	
	private static Pair getPairG(double[] relevances){
		
		Random rand = new Random();
		int listSize = zeros.size();
		while (true) {
			if (listSize == 0) {
				return null;
			}
			Pair pair = new Pair();
			int ix = rand.nextInt(listSize);
			List<Integer> ones = new ArrayList<Integer>();
			for (int i = 0; i < ix; i++) {
				if (relevances[i] > 0) {
					ones.add(i);
				}
			}
			
			if (ones.size() > 0) {
				int j = rand.nextInt(ones.size());
				pair.ri = zeros.get(ix);
				pair.rj = ones.get(j);
				pair.i = ix;
				pair.j = j;
				return pair;
			}
		}
		
	}
	
	public static double[] apply(String[] docnos,  double[] scores) {
		
		double[] relevances = trecUser.get(docnos);
		
		double[] localScore = new double[scores.length];
		
		for (int i = 0; i < scores.length; i++) {
			localScore[i] = scores[i];
		}
		
		makeLists(relevances);
		
		currentAP = computeAP(relevances);
		double smallestDiff = Math.abs(targetAP-currentAP);
		int i = 0;
		while (Math.abs(targetAP-currentAP) > 0.005 && i < 1000) {
		
			Pair pair = null ;
			
			
			if (currentAP > targetAP) {
				pair = getPairG(relevances);
			} else {
				pair = getPairL(relevances);
				
			}
			
			
			double aux = localScore[pair.ri];
			localScore[pair.ri] = localScore[pair.rj];
			localScore[pair.rj] = aux;
			
			aux = relevances[pair.ri];
			relevances[pair.ri] = relevances[pair.rj];
			relevances[pair.rj] = aux;
			
			double candidateAP = computeAP(relevances);
			
			
			if (Math.abs(targetAP-candidateAP) < smallestDiff) {
				smallestDiff = Math.abs(targetAP-candidateAP);
				currentAP = candidateAP;
				zeros.remove(pair.i);
				zeros.add(pair.j);
			} else {
				aux = localScore[pair.ri];
				localScore[pair.ri] = localScore[pair.rj];
				localScore[pair.rj] = aux;
				aux = relevances[pair.ri];
				relevances[pair.ri] = relevances[pair.rj];
				relevances[pair.rj] = aux;	
			}
			i++;
		}
		return localScore;
	}
	
	public static double[] apply(double[] scores) {
		
		double[] localScores = new double[scores.length];
		for (int i = 0; i < localScores.length; i++) {
			localScores[i] = scores[i];
		}
		
		makeLists(localScores);
		
		currentAP = computeAP(localScores);
		double smallestDiff = Math.abs(targetAP-currentAP);
		int i = 0;
		while (Math.abs(targetAP-currentAP) > 0.005 && i < 1000) {
		
			Pair pair = null ;
			
			if (currentAP > targetAP) {
				pair = getPairG(localScores);
			} else {
				pair = getPairL(localScores);
				
			}
			double aux = localScores[pair.ri];
			localScores[pair.ri] = localScores[pair.rj];
			localScores[pair.rj] = aux;
			

			double candidateAP = computeAP(localScores);
			
			if (Math.abs(targetAP-candidateAP) < smallestDiff) {
				smallestDiff = Math.abs(targetAP-candidateAP);
				currentAP = candidateAP;
				zeros.remove(pair.i);
				zeros.add(pair.j);
			} else {
				aux = localScores[pair.ri];
				localScores[pair.ri] = localScores[pair.rj];
				localScores[pair.rj] = aux;
			}
			i++;
		}
		return localScores;
	}
	
	public static double computeAP(double[] relevances){
		double averagePrecision = 0;
		for (int i = 0; i < relevances.length; i++) {
			double precision = 0;
			for (int j = 0; j <= i; j++) {
				precision += relevances[j] > 0 ? 1 : 0;
			}
			precision /= (i+1);
			averagePrecision = relevances[i] > 0 ? averagePrecision+precision : averagePrecision;
			
		}
		averagePrecision /= relevances.length;
		return averagePrecision;
	}
	
	public static class Pair {
		public int ri;
		public int rj;
		public int i;
		public int j;
	}

}
