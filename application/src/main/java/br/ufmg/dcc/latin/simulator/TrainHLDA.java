package br.ufmg.dcc.latin.simulator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import br.ufmg.dcc.latin.searcher.ResultSet;
import br.ufmg.dcc.latin.searcher.SearchResource;
import cc.mallet.pipe.CharSequence2TokenSequence;
import cc.mallet.pipe.Pipe;
import cc.mallet.pipe.SerialPipes;
import cc.mallet.pipe.TokenSequence2FeatureSequence;
import cc.mallet.pipe.TokenSequenceLowercase;
import cc.mallet.pipe.TokenSequenceRemoveNonAlpha;
import cc.mallet.pipe.TokenSequenceRemoveStopwords;
import cc.mallet.pipe.iterator.ArrayIterator;
import cc.mallet.topics.HierarchicalLDA;
import cc.mallet.topics.HierarchicalLDAInferencer;
import cc.mallet.types.InstanceList;
import cc.mallet.util.Randoms;

public class TrainHLDA {

	public static void main(String[] args) {

		
    	String topicsFile = "src/main/resources/topics_domain_2016.txt";
    	SearchResource searchResource = new SearchResource("", "doc");

		String[] fields = {"text","title","anchor"};
		float[] weights = {0.6f,0.3f,0.1f};

		try (BufferedReader br = new BufferedReader(new FileReader(topicsFile))) {
		    String line;
		    FileWriter fstream = new FileWriter("HDLA_topics_more");
		    BufferedWriter out = new BufferedWriter(fstream);
		    while ((line = br.readLine()) != null) {

		    	String[] splitLine = line.split(" ",3);
	        	String indexName = splitLine[0];
	        	if (indexName.equals("Ebola")) {
	        		indexName = "ebola_2016";
				} else if (indexName.equals("Polar")){
					indexName = "polar_2016";
				}
	        	
	        	String topicId = splitLine[1];
	        //	if (!topicId.equals("DD16-2")){
	        //		continue;
	        //	}
	        	
	    		String query = StringEscapeUtils.escapeJava(splitLine[2]);
	    		query = query.replaceAll("/", " ");
	    		System.out.println(topicId);
	    		searchResource.setIndexName(indexName);
	    		
	        	ResultSet resultSet = searchResource.search(query, fields, weights, 1000);
	        	List<String> texts = new ArrayList<String>();
	        	String[] docsContent = resultSet.getDocContent();
	        	for (int i = 0; i < docsContent.length; i++) {
					texts.add(docsContent[i]);
				}
	        	
	        	
	        	
	        	InstanceList instanceList = createInstanceList(texts);
	        	HierarchicalLDA model = new HierarchicalLDA();
	        	
	    		//set parameter
	    		model.setAlpha(10.0);
	    		model.setGamma(1.0);
	    		model.setEta(1.0);
	    		model.setProgressDisplay(false);
	    		
	    		
	    		//set level
	    		model.initialize(instanceList, instanceList, 3, new Randoms());
	        
	    	
	    		model.estimate(3000);
	    		
	    		
	    		HierarchicalLDAInferencer inferencer = new HierarchicalLDAInferencer(model);
	    		
	    		StringBuffer buff = new StringBuffer();
	    
	    		inferencer.printNodeTree(inferencer.getRootNode(), buff,topicId);
	    		out.write(buff.toString());
                out.flush();
                
		    }
 		   	out.close();
 		   	fstream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
	
	private static InstanceList createInstanceList(List<String> texts) throws IOException {
		ArrayList<Pipe> pipes = new ArrayList<Pipe>();
		
	    pipes.add(new CharSequence2TokenSequence());
	    pipes.add(new TokenSequenceLowercase());
	    pipes.add(new TokenSequenceRemoveStopwords());
	    pipes.add(new TokenSequenceRemoveNonAlpha());
	    pipes.add(new TokenSequence2FeatureSequence());
	    InstanceList instanceList = new InstanceList(new SerialPipes(pipes));
	    instanceList.addThruPipe(new ArrayIterator(texts));
	    
	    return instanceList;
		
	}

}
