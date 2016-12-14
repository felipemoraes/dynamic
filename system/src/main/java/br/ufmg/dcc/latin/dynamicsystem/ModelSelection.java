package br.ufmg.dcc.latin.dynamicsystem;

import java.io.IOException;
import java.util.List;

import br.ufmg.dcc.latin.learning.OnlineLeanerFactory;
import br.ufmg.dcc.latin.learning.OnlineLearner;

public class ModelSelection {
	

	public static void main(String[] args) throws IOException {
		String trainingFilename = args[0];
		String validationFilename = args[1];
		String modelFile = args[2];
		
		TrecUser.load("../share/truth_data.txt");
		
		double[] bestParam = null;
		double[] bestWeights = null;
		double bestScore = -1;
		OnlineLearner learner = OnlineLeanerFactory.getInstance(args[3]);
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
				learner.dumpModel(modelFile, bestParam,bestWeights);
			}
			i++;
			System.out.println("Best score: " + score);
			System.out.println("Processed " + i + " of " + params.size() );
		}
		learner.dumpModel(modelFile, bestParam,bestWeights);
	}

}
