package br.ufmg.dcc.latin.models.fsdm;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Scanner;

public class Evaluator {
	final static Charset ENCODING = StandardCharsets.UTF_8;
	
	private HashMap<String, LinkedHashSet<String>> qrels;
	private HashMap<String, ArrayList<String>> ranking;
	
	public Evaluator(){
		
	}
	
	public Evaluator(HashMap<String, LinkedHashSet<String>> qrels){
		this.qrels = qrels;
	}
	
	public Evaluator(HashMap<String, LinkedHashSet<String>> qrels, HashMap<String, ArrayList<String>> ranking){
		this.qrels = qrels;
		this.ranking = ranking;
	}
	
	public HashMap<String, LinkedHashSet<String>> getQrels(){
		return qrels;
	}
	
	public void parseQrelsFile(String filePath){
		qrels = new HashMap<String, LinkedHashSet<String>>();
		try{
			Path path = Paths.get(filePath);
			String line;
			LinkedHashSet<String> aux;
		    try(Scanner scanner =  new Scanner(path, ENCODING.name())){
	    		while (scanner.hasNextLine()){
					line = scanner.nextLine();

					String[] parts = line.split("\t");
					String queryId = parts[0];
					String url = parts[2];
					if(qrels.containsKey(queryId)){
						aux = qrels.get(queryId);
						aux.add(url);
						qrels.put(queryId, aux);
					}
					else{
						aux = new LinkedHashSet<String>();
						aux.add(url);
						qrels.put(queryId, aux);
					}
					
		    	}
		    }
		}
		catch(Exception e){
			System.out.println("Erro ao processar arquivo de qrels: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public void parseTrecEvalFile(String filePath){
		ranking = new HashMap<String, ArrayList<String>>();
		try{
			Path path = Paths.get(filePath);
			String line;
			ArrayList<String> aux;
		    try(Scanner scanner =  new Scanner(path, ENCODING.name())){
	    		while (scanner.hasNextLine()){
					line = scanner.nextLine();

					String[] parts = line.split("\t");
					String queryId = parts[0];
					String url = parts[2];
					if(ranking.containsKey(queryId)){
						aux = ranking.get(queryId);
						aux.add(url);
						ranking.put(queryId, aux);
					}
					else{
						aux = new ArrayList<String>();
						aux.add(url);
						ranking.put(queryId, aux);
					}
					
		    	}
		    }
		}
		catch(Exception e){
			System.out.println("Erro ao processar arquivo do treceval: "+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public double precisionByQueryAt(String queryId, int num){
		double precision = 0;
		
		int i = 1;
		for(String item : ranking.get(queryId)){
			if(qrels.get(queryId).contains(item)){
				precision++;
			}
			i++;
			
			if(i>num)
				break;
		}
		
		return precision / num;
	}
	
	public double precisionByQuery(String queryId){
		double precision = 0;
		
		for(String item : ranking.get(queryId)){
			if(qrels.get(queryId).contains(item)){
				precision++;
			}
		}
		
		return precision / ranking.size();
	}
	
	public double precision(){
		double precision = 0;
		int counter = 0;
		for(String queryId : ranking.keySet()){
			precision += precisionByQuery(queryId);
			counter++;
		}
		
		return precision / counter;
	}
	
	public double precisionAt(int num){
		double precision = 0;
		int counter = 0;
		for(String queryId : ranking.keySet()){
			precision += precisionByQueryAt(queryId, num);
			counter++;
		}
		
		return precision / counter;
	}
	
	public double recallByQuery(String queryId){
		double recall = 0;
		
		for(String item : ranking.get(queryId)){
			if(qrels.get(queryId).contains(item)){
				recall++;
			}
		}
		
		return recall / qrels.get(queryId).size();
	}
	
	public double recall(){
		double recall = 0;
		
		int counter = 0;
		for(String queryId : ranking.keySet()){
			recall += recallByQuery(queryId);
			counter++;
		}
		
		return recall / counter;
	}
	
	public double averagePrecisionByQuery(String queryId){
		double avg = 0;
		double relevants = 0;
		double position = 0;
		
		for(String item : ranking.get(queryId)){
			position++;
			if(qrels.get(queryId).contains(item)){
				relevants++;
				avg += (double) relevants / position;
			}
			
		}
		
		return avg / qrels.get(queryId).size();
	}
	
	public double map(HashMap<String, ArrayList<String>> ranking){
		this.ranking = ranking;
		return map();
	}
	
	public double map(){
		double map = 0;
		
		double counter = 0;
		for(String queryId : ranking.keySet()){
			map += averagePrecisionByQuery(queryId);
			counter++;
		}
		
		return map / counter;
	}
	
}
