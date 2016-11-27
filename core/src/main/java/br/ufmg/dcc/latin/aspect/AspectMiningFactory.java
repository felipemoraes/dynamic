package br.ufmg.dcc.latin.aspect;

public class AspectMiningFactory {
	
	public static AspectMining getInstance(String className){
		AspectMining aspectMining = null;

		if (className.equals("PassageAspectMining")){
			aspectMining = new PassageAspectMining();
		} else if (className.equals("WeightedPassageAspectMining")){
			aspectMining = new WeightedPassageAspectMining();
		} else if (className.equals("SubtopicNameAspectMining")){
			aspectMining = new SubtopicNameAspectMining();
		} else if (className.equals("MostRelevantTermsAspectMining")){
			aspectMining = new MostRelevantTermsAspectMining();
		}
		
		return aspectMining;
	}
}
