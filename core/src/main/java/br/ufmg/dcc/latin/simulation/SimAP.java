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
	
	private static List<Pair> iGreaterThanj;
	private static List<Pair> jGreaterThani;
	
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
		
		iGreaterThanj = new ArrayList<Pair>();
		jGreaterThani = new ArrayList<Pair>();

		for (int i = 0; i < relevances.length; i++) {
			if (relevances[i] == 0) {
				for (int j = 0; j < relevances.length; j++) {
					if (i == j) {
						continue;
					}
					if (relevances[j] > 0) {
						Pair pair = new Pair();
						pair.ri = i;
						pair.rj = j;
						if ( i > j) {
							iGreaterThanj.add(pair);
						} else {
							jGreaterThani.add(pair);
						}
					}
				}
			}
		}
	}
	public static double[] apply(String[] docnos,  double[] scores) {
		double[] relevances = trecUser.get(docnos);
		
		Random rand = new Random();
		makeLists(relevances);
		
		currentAP = computeAP(relevances);
		double smallestDiff = Math.abs(targetAP-currentAP);
		int i = 0;
		while (Math.abs(targetAP-currentAP) > 0.0005 && i < 10000) {
		
			Pair pair = null ;
			int listSizei = iGreaterThanj.size();
			int listSizej = jGreaterThani.size();
			if (currentAP > targetAP) {
				if (listSizei == 0) {
					break;
				}
				int choose = rand.nextInt(listSizei);
				pair = iGreaterThanj.get(choose);

						
			} else {
				if (listSizej == 0) {
					break;
				}
				int choose = rand.nextInt(listSizej);
				pair = jGreaterThani.get(choose);
			}
			
			double aux = scores[pair.ri];
			scores[pair.ri] = scores[pair.rj];
			scores[pair.rj] = aux;
			
			aux = relevances[pair.ri];
			relevances[pair.ri] = relevances[pair.rj];
			relevances[pair.rj] = aux;
			
			double candidateAP = computeAP(relevances);
			
			
			if (Math.abs(targetAP-candidateAP) < smallestDiff) {
				smallestDiff = Math.abs(targetAP-candidateAP);
				currentAP = candidateAP;
				makeLists(relevances);
			} else {
				aux = scores[pair.ri];
				scores[pair.ri] = scores[pair.rj];
				scores[pair.rj] = aux;
				aux = relevances[pair.ri];
				relevances[pair.ri] = relevances[pair.rj];
				relevances[pair.rj] = aux;	
			}
			i++;
		}
		return scores;
	}
	
	public static double[] apply(double[] scores) {
		
		Random rand = new Random();
		makeLists(scores);
		
		currentAP = computeAP(scores);
		double smallestDiff = Math.abs(targetAP-currentAP);
		int i = 0;
		while (Math.abs(targetAP-currentAP) > 0.0005 && i < 10000) {
		
			Pair pair = null ;
			int listSizei = iGreaterThanj.size();
			int listSizej = jGreaterThani.size();
			if (currentAP > targetAP) {
				if (listSizei == 0) {
					break;
				}
				int choose = rand.nextInt(listSizei);
				pair = iGreaterThanj.get(choose);

						
			} else {
				if (listSizej == 0) {
					break;
				}
				int choose = rand.nextInt(listSizej);
				pair = jGreaterThani.get(choose);
			}
			
			double aux = scores[pair.ri];
			scores[pair.ri] = scores[pair.rj];
			scores[pair.rj] = aux;
			
			
			double candidateAP = computeAP(scores);
			
			
			if (Math.abs(targetAP-candidateAP) < smallestDiff) {
				smallestDiff = Math.abs(targetAP-candidateAP);
				currentAP = candidateAP;
				makeLists(scores);
			} else {
				aux = scores[pair.ri];
				scores[pair.ri] = scores[pair.rj];
				scores[pair.rj] = aux;
			}
			i++;
		}
		return scores;
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
	}

}
