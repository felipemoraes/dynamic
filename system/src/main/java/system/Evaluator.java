package system;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.querying.ResultSet;

public class Evaluator {
	
	public static void writeToFile(String runName, String topicId, ResultSet resultSet, int iteration){
		try(FileWriter fw = new FileWriter(runName + ".txt", true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {
			String[] docnos = resultSet.getDocnos();
			for (int i = 0; i < docnos.length; i++) {
				float score = (float) ((1000.0-iteration-i)/1000.0);
				String wline = topicId + "\t" + iteration +"\t" + docnos[i] + "\t" + String.format("%.12f", score) + "\t";
				Feedback feedback = TrecUser.get(docnos[i], topicId);
				if (feedback.isOnTopic()){
					wline += "0\tNULL";
				} else {
					wline += "1" +  "\t";
					for (int j = 0; j < feedback.getPassages().length; j++) {
						wline += feedback.getPassages()[i].getAspectId() + ":" + feedback.getPassages()[i].getRelevance() + "|";
					}
					wline = wline.substring(0,wline.length()-1);
				}
				out.println(wline);
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
