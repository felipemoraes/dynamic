package br.ufmg.dcc.latin.learning;

public class LearnerFactory {
	public static Learner getInstance(String onlineLearner){
		System.out.println(onlineLearner);
		if (onlineLearner.equals("DBGD")){
			return new DBGD();
		} else if (onlineLearner.equals("Static")) {
			return new Static();
		} else if (onlineLearner.equals("CA")) {
			return new CoordinateAscent();
		}
		return null;
	}
}
