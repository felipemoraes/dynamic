package br.ufmg.dcc.latin.utils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.user.UserQuery;

public class TopicsFile {
	
	public static List<UserQuery> getTrecDD(){
		String topicsFile = "../share/topics_domain.txt";
		List<UserQuery> topics = new ArrayList<UserQuery>();
		
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(topicsFile));
		    while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ",3);
		    	String index = splitLine[0];
		    	String topicId = splitLine[1];
				String query = splitLine[2].replaceAll("/", " ");
				UserQuery userQuery = new UserQuery();
				userQuery.index = index;
				userQuery.tid = topicId;
				userQuery.query = query;
				topics.add(userQuery);
		    }
		    br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return topics;
	
		
	}

}
