package br.dcc.latin.searcher;

import java.util.Arrays;


import org.junit.Assert;
import org.junit.Test;

import br.ufmg.dcc.latin.models.xQuADi;

public class xQuADiTest {

	@Test
	public void test() {
		int[] docids = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
		double[] relevance = {0.95,0.9,0.85,0.80,0.75,   0.70,0.70,0.60,0.60,0.50,   0.4999,0.30,0.30,0.20,0.10};
		
		double[] importance = new double[100];
		Arrays.fill(importance, -1);

		
		double[][] coverage = { {0.60,0.00},{0.50,0.00},{0.00,0.00},{0.00,0.00},{0.10,0.00}, 
				{0.30,0.40},{0.70,0.60},{0.20,0.30},{0.70,0.80},{0.10,0.30},
				{0.90,0.60},{0.70,0.60},{0.20,0.30},{0.70,0.80},{0.40,0.20},
		};
		
		importance[0] = 0.60;
		importance[1] = 0.40;
		
		
		xQuADi xq = new xQuADi();
		double[] rank = xq.score(docids,relevance, coverage,importance , 0.5, 5, 0);
		for (int i = 0; i < rank.length; i++) {
			System.out.print(docids[i] + ":" + rank[i] + " ");
		}
		System.out.println();
		System.out.println();
		
		
		rank = xq.score(docids,relevance, coverage,importance , 0.9, 5, 1);
		for (int i = 0; i < rank.length; i++) {
			System.out.print(docids[i] + ":" + rank[i] + " ");
		}
		System.out.println();
		System.out.println();
		
		rank = xq.score(docids,relevance, coverage,importance , 0.9, 5,2);
		for (int i = 0; i < rank.length; i++) {
			System.out.print(docids[i] + ":" + rank[i] + " ");
		}
		System.out.println();
		Assert.assertEquals(docids.length, relevance.length);
	}

}
