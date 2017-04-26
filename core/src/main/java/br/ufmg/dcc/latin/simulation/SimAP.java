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
	
	private static Random rand = new Random();
	
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
	
	private static double computeBestAP(double[] relevances){
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
		return bestAP;
	}
	
	private static Pair chooseSwap(double[] relevances, boolean improve){
		Random rand = new Random();
		int i = -1;
		int j = -1;
		boolean OK = false;
		int attempt = 0;
		while (!OK) {
			if (improve) {
				j = rand.nextInt(relevances.length);
				if ( j == 0) {
					continue;
				}
				i = rand.nextInt(j);
			} else {
				i = rand.nextInt(relevances.length);
				if ( i == 0) {
					continue;
				}
				j = rand.nextInt(i);
			}
			attempt++;
			if (attempt % 100 == 0) {
				//System.out.println("randLoop, attempt " + attempt);
			}
			OK = (relevances[i] == 0 ) && (relevances[j] > 0);
		}
		
		Pair pair = new Pair();
		pair.ri = i;
		pair.rj = j;
		return pair;
	}
	
	private static Pair chooseSwap2(double[] relevances, boolean improve){
		
		int highestRel = -1;
		int highestIrrel = -1;
		int lowestRel = -1;
		int lowestIrrel = -1;
		int index =0;
		
		int i = -1;
		int j = -1;

		//find starting indices
		while(highestRel == -1 || highestIrrel == -1) {
			if (index >= relevances.length) {
				Pair pair = new Pair();
				pair.ri = i;
				pair.rj = j;
				return pair;
			}
			highestIrrel = (relevances[index] == 0 && highestIrrel == -1) ? index : highestIrrel;
			highestRel =  (relevances[index] != 0 && highestRel == -1) ? index : highestRel ;
			index++;
		}
		index = -1 + relevances.length;
		while(lowestRel == -1 || lowestIrrel == -1) {
			if (index < 0) {
				Pair pair = new Pair();
				pair.ri = i;
				pair.rj = j;
				return pair;
			}
			lowestIrrel =  (relevances[index]== 0 && lowestIrrel == -1) ? index : lowestIrrel;
			lowestRel =  (relevances[index] != 0 && lowestRel == -1) ? index : lowestRel;
			index--;
		}
		
		if (improve) {
			// no more improving moves that we can make
			if (highestIrrel > lowestRel) {
				Pair pair = new Pair();
				pair.ri = i;
				pair.rj = j;
				return pair;
			}
			//improve
		    // find j where r_j >0
			int tryA = 0;
			do {
				// select a document before the lowestRelevant one, but after the highest irrelevant one, so
				// that we have room to find an i value which might lead to a irrelevant document
				j = highestIrrel+ rand.nextInt(lowestRel-highestIrrel) +1;
				tryA++;
				if (tryA % 100 == 0) {
				//	System.out.println("Looking for relevant document after rank of highest irrelevant"
				//+ " document at rank " + highestIrrel + ". lowestRel=" + lowestRel+". checking j=" + j +  ", try=" + tryA);
					
				}
			} while ( relevances[j] == 0);
			
			// find i where r_i == 0 and i<j
			int tryB = 0;
			do {
				i = rand.nextInt(j);
				tryB++;
				if (tryB % 100 == 0) {
				//	System.out.println("Looking for irrelevant document above rank j="+ j 
				//			+", highestIrrel is "+highestIrrel+", checking " + i + ", try=" + tryB);
				}
			} while ( relevances[i] > 0);
		} else {
			// disimprove
			
			int tryA = 0;
			
			do{
				// select an irrelevant document before the lowestIrrelevant one, but after the highest relevant one, so
				// that we have room to find a j value which might lead to a relevant document
				if (highestRel > lowestIrrel) {
					Pair pair = new Pair();
					pair.ri = i;
					pair.rj = j;
					return pair;
				}
				i =  highestRel+  rand.nextInt(lowestIrrel-highestRel)+1;
				tryA++;
			//	System.out.println("Looking for irrelevant document after rank j="+ j 
			//			+", highestIrrel is "+highestIrrel+", checking " + i + ", try=" + tryA);
	        } while (relevances[i] != 0 );
			
			// find j where r_j >0  and j < i
			int tryB = 0;
			do{
	            j = rand.nextInt(i);
				tryB++;
			//	System.out.println("Looking for relevant document above rank i=$i, highestRel is $highestRel, checking $j, try="  + tryB);
				 
	        }while(relevances[j]  == 0);
			
			
		}
		
		Pair pair = new Pair();
		pair.ri = i;
		pair.rj = j;
		return pair;
	}
	

	public static double[] apply(String[] docnos,  double[] scores) {
		
		double[] relevances = TrecUser.get(docnos);
		
		double[] localScore = new double[scores.length];
		
		for (int i = 0; i < scores.length; i++) {
			localScore[i] = scores[i];
		}
		
		
	    //  fisher_yates_shuffle( \@array ) : generate a random permutation
		// of @array in place
		for (int i = localScore.length-1; i >= 1; i--) {
			int j = rand.nextInt(i+1);
			
			double aux = localScore[i];
			
			localScore[i] = localScore[j];
			localScore[j] = aux;
			
			aux = relevances[i];
			relevances[i] = relevances[j];
			relevances[j] = aux;
		}

		currentAP = computeAP(relevances);
		
		double bestAP = computeBestAP(relevances);
		
		double smallestDiff = Math.abs(targetAP-currentAP);
		int i = 0;
		while (Math.abs(targetAP-currentAP) > 0.005 && i < 1000) {
		
			
			Pair pair = chooseSwap2(relevances, currentAP < targetAP) ;
			 
			if (pair.rj == -1) {
				break;
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
			} else {
				aux = localScore[pair.ri];
				localScore[pair.ri] = localScore[pair.rj];
				localScore[pair.rj] = aux;
				aux = relevances[pair.ri];
				relevances[pair.ri] = relevances[pair.rj];
				relevances[pair.rj] = aux;	
			}
			i++;
			
			if (bestAP == currentAP){
				break;
			}
		}
		return localScore;
	}
	
	public static double[] apply(int[] ids, double[] scores) {
		
		double[] localScores = new double[scores.length];
		int[] localIds = new int[ids.length];
		for (int i = 0; i < localScores.length; i++) {
			localScores[i] = scores[i];
			localIds[i] = ids[i];
		}
		
	    //  fisher_yates_shuffle( \@array ) : generate a random permutation
		// of @array in place
		
		for (int i = localScores.length-1; i >= 1; i--) {
			int j = rand.nextInt(i+1);
			double aux = localScores[i];
			localScores[i] = localScores[j];
			localScores[j] = aux;
			int auxid = localIds[i];
			localIds[i] =  localIds[j];
			localIds[j] = auxid;
 		}
		
		currentAP = computeAP(localScores);
		
		double bestAP = computeBestAP(localScores);
		
		double smallestDiff = Math.abs(targetAP-currentAP);
		int i = 0;
		while (Math.abs(targetAP-currentAP) > 0.005 && i < 1000) {
			
			Pair pair = chooseSwap2(localScores, currentAP < targetAP) ;
			 
			if (pair.rj == -1) {
				break;
			}
			
			double aux = localScores[pair.ri];
			localScores[pair.ri] = localScores[pair.rj];
			localScores[pair.rj] = aux;
			
			int auxid = localIds[pair.ri];
			localIds[pair.ri] =  localIds[pair.rj];
			localIds[pair.rj] = auxid;

			double candidateAP = computeAP(localScores);
			
			if (Math.abs(targetAP-candidateAP) < smallestDiff) {
				smallestDiff = Math.abs(targetAP-candidateAP);
				currentAP = candidateAP;
			} else {
				aux = localScores[pair.ri];
				localScores[pair.ri] = localScores[pair.rj];
				localScores[pair.rj] = aux;
				
				auxid = localIds[pair.ri];
				localIds[pair.ri] =  localIds[pair.rj];
				localIds[pair.rj] = auxid;
			}
			i++;
			
			if (bestAP == currentAP){
				break;
			}
			
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
	}

}
