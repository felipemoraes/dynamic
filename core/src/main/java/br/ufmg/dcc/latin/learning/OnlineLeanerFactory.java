package br.ufmg.dcc.latin.learning;

public class OnlineLeanerFactory {
	public static OnlineLearner getInstance(String onlineLearner){
		System.out.println(onlineLearner);
		if (onlineLearner.equals("DBGD")){
			return new DBGD();
		} else if (onlineLearner.equals("Static")) {
			return new Static();
		}
		return null;
	}
}
