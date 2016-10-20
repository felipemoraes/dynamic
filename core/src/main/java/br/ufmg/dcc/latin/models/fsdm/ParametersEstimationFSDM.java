package br.ufmg.dcc.latin.models.fsdm;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ParametersEstimationFSDM {
	
	private QueryProcessor processor = new QueryProcessor();
	private Evaluator evaluator = new Evaluator();
	private FSDMTester tester;
	private ArrayList<String> queryIds = new ArrayList<String>();
	
	
	public ParametersEstimationFSDM(String queriesFilePath, String qrelsFilePath, String dataFolderPath){
		processor.parseQueriesFile(queriesFilePath);
		HashMap<String, String> queriesMap = processor.getQueries();
		
		queryIds = new ArrayList<String>(queriesMap.keySet());
		tester = new FSDMTester();
		tester.loadAllDataToMemory(dataFolderPath, queryIds);
		evaluator.parseQrelsFile(qrelsFilePath);
	}
	
	public ArrayList<ArrayList<String>> createFolds(ArrayList<String> queryIds){
		int numFolds = 5;
		
		//Collections.shuffle(queryIds, new Random(System.nanoTime()));
		int chunkSize = (queryIds.size() - (queryIds.size() % numFolds)) / numFolds;
		ArrayList<ArrayList<String>> folds = new ArrayList<ArrayList<String>>(); 
		
		for(int i=0; i<5; i++){
			int from = chunkSize * i;
			int to = from + chunkSize;
			folds.add(new ArrayList<String>(queryIds.subList(from, to)));
		}
		
		return folds;
	}
	
	public double[] sumNormalization(double[] weights){
		double total = 0;
		
		for(int i=0; i<weights.length; i++)
			total += weights[i];
		
		for(int i=0; i<weights.length; i++)
			weights[i] = weights[i] / total;
		
		return weights;
	}
	
	public double[] randomWeights(int numFields){
		int max = 10;
		int min = 0;
		double[] weights = new double[numFields];
		Random random = new Random();
		
		for(int i=0; i<numFields; i++){
			weights[i] = random.nextInt(max - min + 1) + min;
		}
		
		return sumNormalization(weights);
	}
	
	public ArrayList<String> separateTrainFolds(ArrayList<ArrayList<String>> folds, int excludeIndex){
		ArrayList<String> trainFolds = new ArrayList<String>();
		
		for(int i=0; i<folds.size(); i++)
			if(i != excludeIndex)
				trainFolds.addAll(folds.get(i));
		
		return trainFolds;
	}
	
	public double mapFromRankings(ArrayList<String> queryIds, String model, double[] weights){
		return evaluator.map(tester.experimentFieldWeights(queryIds, model, weights));
	}
	
	public double mapFromRankings(ArrayList<String> queryIds, double[] alphaWeights, double[] uWeights, double[] bWeights, double[] wbWeights){
		return evaluator.map(tester.experimentModelWeights(queryIds, alphaWeights, uWeights, bWeights, wbWeights));
	}
	
	public void coordinateAscentForWs(String model){
		ArrayList<ArrayList<String>> folds = createFolds(queryIds);
		int numAttemps = 5;
		double[] avgWeights = new double[]{0, 0, 0, 0, 0};
		
		for(int i=0; i<folds.size(); i++){
			ArrayList<String> trainFolds = separateTrainFolds(folds, i);
			double bestMap = 0;
			double[] bestWeights = new double[]{0, 0, 0, 0, 0};
			for(int j=0; j<numAttemps; j++){
				double[] weights = randomWeights(5);
				System.out.print("chute inicial: "+weights[0]+", "+weights[1]+", "+weights[2]+", "+weights[3]+", "+weights[4]);
				double[] newWeights = coordinateAscentForWs(trainFolds, model, weights);
				double newMap = mapFromRankings(folds.get(i), model, newWeights);
				System.out.println(" = "+newMap);
				
				if(newMap > bestMap){
					bestMap = newMap;
					bestWeights = newWeights;
				}
			}
			for(int x=0; x<bestWeights.length; x++)
				avgWeights[x] += bestWeights[x];
			
			System.out.println("fold "+i+" - "+bestMap);
		}
		
		for(int x=0; x<avgWeights.length; x++)
			avgWeights[x] = avgWeights[x] / (double) folds.size();
		
		avgWeights = sumNormalization(avgWeights);
		
		System.out.print("Final weights: "+avgWeights[0]+", "+avgWeights[1]+", "+avgWeights[2]+", "+avgWeights[3]+", "+avgWeights[4]);
		System.out.println(" = "+mapFromRankings(queryIds, model, avgWeights));
	}
	
	
	/**
	 * Estimates field weights for an n-gram model
	 * @param queryIds queries used on training
	 * @param model Name of the model to use: "unigrams", "bigrams" or "wbigrams"
	 * @param weights initial field weights
	 */
	public double[] coordinateAscentForWs(ArrayList<String> queryIds, String model, double[] weights){
		int index = 0;
		double step = 0.05;
		double bestMap = evaluator.map(tester.experimentFieldWeights(queryIds, model, weights));
		double bestParam = weights[index];
		double newMap = 0.0;
		double bestForNow = 0.0;
		boolean runAllParams = true;
		
		//System.out.println("Initial map: "+bestMap);
		
		int k = 0;
		while(runAllParams){//for(int k=0; k<=15; k++){
			k++;
			//System.out.println(k+"-----------------------------------------------");
			bestForNow = 0;
			for(int j=0; j<weights.length; j++){
				index = j;
				bestParam = weights[index];
				boolean shouldContinue = true;
				while(shouldContinue){//for(int i=0; i<=20; i++){
					weights[index] += step;
					
					/*for(double v : weights)
						System.out.print(round(v, 2)+" ");
					System.out.print(" - ");*/
					
					HashMap<String, ArrayList<String>> rankings = tester.experimentFieldWeights(queryIds, model, weights);
					newMap = evaluator.map(rankings);
					if(newMap > bestMap){
						bestMap = newMap;
						bestParam = weights[index];
					}
					else{
						shouldContinue = false;
					}
					//System.out.println(newMap);
					
					if(newMap > bestForNow)
						bestForNow = newMap;
				}
				
				weights[index] = bestParam;
			}
			if(bestForNow < bestMap || k > 10)
				runAllParams = false;
			//System.out.println("Melhor nessa rodada: "+bestForNow+" global: "+bestMap);
		}
		
		/*System.out.print("melhores: ");
		for(double v : weights)
			System.out.print(v+" ");
		System.out.print(" - ");
		System.out.println(bestMap);*/
		
		return weights;
		
	}
	
	public void coordinateAscentForAlphas(double[] uWeights, double[] bWeights, double[] wbWeights){
		ArrayList<ArrayList<String>> folds = createFolds(queryIds);
		int numAttemps = 5;
		double[] avgWeights = new double[]{0, 0, 0};
		
		for(int i=0; i<folds.size(); i++){
			ArrayList<String> trainFolds = separateTrainFolds(folds, i);
			double bestMap = 0;
			double[] bestWeights = new double[]{0, 0, 0};
			for(int j=0; j<numAttemps; j++){
				double[] aWeights = randomWeights(3);
				System.out.print("chute inicial: "+aWeights[0]+", "+aWeights[1]+", "+aWeights[2]);
				double[] newWeights = coordinateAscentForAlphas(trainFolds, uWeights, bWeights, wbWeights, aWeights);
				double newMap = mapFromRankings(folds.get(i), newWeights, uWeights, bWeights, wbWeights);
				System.out.println(" = "+newMap);
				
				if(newMap > bestMap){
					bestMap = newMap;
					bestWeights = newWeights;
				}
			}
			for(int x=0; x<bestWeights.length; x++)
				avgWeights[x] += bestWeights[x];
			
			System.out.println("fold "+i+" - "+bestMap);
		}
		
		for(int x=0; x<avgWeights.length; x++)
			avgWeights[x] = avgWeights[x] / (double) folds.size();
		
		avgWeights = sumNormalization(avgWeights);
		
		System.out.print("Final weights: "+avgWeights[0]+", "+avgWeights[1]+", "+avgWeights[2]);
		System.out.println(" = "+mapFromRankings(queryIds, avgWeights, uWeights, bWeights, wbWeights));
	}
	
	
	/**
	 * Estimates alpha weights of FSDM formula
	 */
	public double[] coordinateAscentForAlphas(ArrayList<String> queryIds, double[] uWeights, double[] bWeights, double[] wbWeights, double[] alphaWeights){
		
		int index = 0;
		double step = 0.05;
		double bestMap = evaluator.map(tester.experimentModelWeights(queryIds, alphaWeights, uWeights, bWeights, wbWeights));
		double bestParam = alphaWeights[index];
		double newMap = 0.0;
		double bestForNow = 0.0;
		boolean runAllParams = true;
		
		System.out.println("Initial map: "+bestMap);
		
		int k = 0;
		while(runAllParams){//for(int k=0; k<=15; k++){
			k++;
			System.out.println(k+"-----------------------------------------------");
			bestForNow = 0;
			for(int j=0; j<alphaWeights.length; j++){
				index = j;
				bestParam = alphaWeights[index];
				boolean shouldContinue = true;
				while(shouldContinue){//for(int i=0; i<=20; i++){
					alphaWeights[index] += step;
					
					//System.out.print("["+dateFormat.format(new Date())+"] "+i+": ");
					for(double v : alphaWeights)
						System.out.print(round(v, 2)+" ");
					System.out.print(" - ");
					
					HashMap<String, ArrayList<String>> rankings = tester.experimentModelWeights(queryIds, alphaWeights, uWeights, bWeights, wbWeights);
					newMap = evaluator.map(rankings);
					if(newMap > bestMap){
						bestMap = newMap;
						bestParam = alphaWeights[index];
					}
					else{
						shouldContinue = false;
					}
					System.out.println(newMap);
					
					if(newMap > bestForNow)
						bestForNow = newMap;
				}
				
				alphaWeights[index] = bestParam;
			}
			if(bestForNow < bestMap || k > 10)
				runAllParams = false;
			System.out.println("Melhor nessa rodada: "+bestForNow+" global: "+bestMap);
		}
		
		System.out.print("melhores: ");
		for(double v : alphaWeights)
			System.out.print(v+" ");
		System.out.print(" - ");
		System.out.println(bestMap);
		
		return alphaWeights;
		
	}
	
	//---------------------------------------------------------
	
	public void adjustJSONs(){
		int i = 1;
		for(String queryId : queryIds){
			
			System.out.println(i+" "+queryId);
			try (BufferedWriter writer = Files.newBufferedWriter(Paths.get("queries_responses_fsdm2/"+queryId+".txt"))) {
				try (BufferedReader br = new BufferedReader(new FileReader("queries_responses_fsdm/"+queryId+".txt"))) {
					JSONObject unigrams = new JSONObject(br.readLine());
					JSONObject bigrams = new JSONObject(br.readLine());
					JSONObject fieldsStats = new JSONObject(br.readLine());
					JSONObject uCollFreq = new JSONObject(br.readLine());
					JSONObject bCollFreq = new JSONObject(br.readLine());
					
					writer.write(unigrams.toString()+"\n");
					writer.write(bigrams.toString()+"\n");
					writer.write(fieldsStats.toString()+"\n");
					writer.write(uCollFreq.toString()+"\n");
					writer.write(bCollFreq.toString()+"\n");
								
					String line;
					while ((line = br.readLine()) != null) {
						JSONObject doc = new JSONObject(line);
						writer.write(doc.toString()+"\n");
					}

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			i++;
		}
	}
	
	
	public ArrayList<double[]> readPossibleModelsWeights(){
		ArrayList<double[]> possibleWeights = new ArrayList<double[]>(); 
		try {
			Ini paramsValues = new Ini(new File("model_weights"));
			Section modelsSection = paramsValues.get("models");
			
			possibleWeights.add(modelsSection.getAll("unigram", double[].class));
			possibleWeights.add(modelsSection.getAll("bigram", double[].class));
			possibleWeights.add(modelsSection.getAll("wbigram", double[].class));
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return possibleWeights;
	}
	
	public ArrayList<double[]> readPossibleFieldsWeights(){
		ArrayList<String> fields = processor.getFields();
		ArrayList<double[]> possibleWeights = new ArrayList<double[]>(); 
		try {
			Ini paramsValues = new Ini(new File("field_weights"));
			Section fieldsSection = paramsValues.get("fields");
			
			for(String field : fields){
				double[] values = fieldsSection.getAll(field, double[].class);
				possibleWeights.add(values);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return possibleWeights;
	}
	
	public ArrayList<double[]>  generatePermutations(ArrayList<double[]> lists){
		ArrayList<double[]> result = new ArrayList<double[]>();
		double[] current = new double[lists.size()];
		generatePermutations(lists, result, 0, current, 0);
		
		return result;
	}
	
	private void generatePermutations(ArrayList<double[]> Lists, ArrayList<double[]> result, int depth, double[] current, double sum){
	    if(depth == Lists.size()){
	    	if(round(sum,2) == 1)
	    		result.add(current);
	        return;
	     }

	    for(int i = 0; i < Lists.get(depth).length; ++i){
	    	double[] temp = current.clone();
	    	temp[depth] = Lists.get(depth)[i];
	        generatePermutations(Lists, result, depth + 1, temp, sum + Lists.get(depth)[i]);
	    }
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = new BigDecimal(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	
}
