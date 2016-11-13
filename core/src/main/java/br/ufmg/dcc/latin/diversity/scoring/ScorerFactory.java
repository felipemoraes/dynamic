package br.ufmg.dcc.latin.diversity.scoring;

public class ScorerFactory {
	
	public static Scorer getInstance(String className){
		Scorer scorer = null;

		if (className.equals("xQuAD")){
			scorer = new xQuAD();
		} else if (className.equals("MMR")){
			scorer = new MMR();
		} else if (className.equals("PM2")){
			scorer = new PM2();
		} else if (className.equals("xMMR")){
			scorer = new xMMR();
		} else if (className.equals("xQuADNaive")){
			scorer = new xQuADNaive();
		} 
		
		return scorer;
	}
	
}
