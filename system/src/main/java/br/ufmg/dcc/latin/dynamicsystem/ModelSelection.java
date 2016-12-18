package br.ufmg.dcc.latin.dynamicsystem;

import java.io.IOException;
import java.util.List;

import br.ufmg.dcc.latin.learning.LearnerFactory;
import br.ufmg.dcc.latin.learning.Learner;

public class ModelSelection {
	

	public static void main(String[] args) throws IOException {
		String trainingFilename = args[0];
		String validationFilename = args[1];
		String modelFile = args[2];
		
		TrecUser.load("../share/truth_data.txt");
		
		double[] bestParam = null;
		double[] bestWeights = null;
		double bestScore = -1;
		Learner learner = LearnerFactory.getInstance(args[3]);
		learner.setupReranker(args[4]);
		learner.loadTrainingSet(trainingFilename);
		learner.loadValidationSet(validationFilename);
		int i = 0;
		List<double[]> params = learner.getParams();
		for (double[] param: params){
			learner.setParam(param);
			double[] weights = learner.train();
			double score = learner.validate(weights);
			System.out.println("Score found: " + score);
			if (score > bestScore) {
				bestScore = score;
				bestParam = param;
				bestWeights = weights;
				learner.dumpModel(modelFile+"_" + args[3] + "_" + args[4], bestParam,bestWeights);
			}
			i++;
			System.out.println("Best score: " + bestScore);
			System.out.println("Processed " + i + " of " + params.size() );
		}
		learner.dumpModel(modelFile, bestParam,bestWeights);
	}

}
