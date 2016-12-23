package br.ufmg.dcc.latin.dynamicsystem;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.user.TrecUser;

public class Evaluator {
	
	public static void writeToFile(String runName, String topicId, ResultSet resultSet, int iteration){
		try(FileWriter fw = new FileWriter("data/" + runName + ".txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {
			String[] docnos = resultSet.docnos;
			for (int i = 0; i < docnos.length; i++) {
				float score = (float) ((1000.0-iteration-i)/1000.0);
				String wline = topicId + "\t" + iteration +"\t" + docnos[i] + "\t" + String.format("%.12f", score) + "\t";
				Feedback feedback = TrecUser.get(docnos[i], topicId);
				if (!feedback.onTopic){
					wline += "0\tNULL";
				} else {
					wline += "1" +  "\t";
					for (int j = 0; j < feedback.passages.length; j++) {
						
						wline += feedback.passages[j].aspectId + ":" + feedback.passages[j].aspectId + "|";
					}
					wline = wline.substring(0,wline.length()-1);
				}
				out.println(wline);
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public static void writeToFile(String runName, String debugMessage){
		try(FileWriter fw = new FileWriter("data/" + runName + "_debug_cov.txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {
			
			out.println(debugMessage);
				
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}


}
