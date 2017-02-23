package br.ufmg.dcc.latin.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;





public class ComputeLengths {

	private static PrintWriter out;

	public static void main(String[] args) throws IOException {
		String topicsFile = "../share/topics_domain.txt";
		Map<String,String> topicDomains = new HashMap<String,String>();
		try {
			String line;
			BufferedReader br = new BufferedReader(new FileReader(topicsFile));
		    while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(" ",3);
		    	String index = splitLine[0];
		    	String topicId = splitLine[1];

				topicDomains.put(topicId, index);
		    }
		    br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String,Set<String>> docIndex = new HashMap<String,Set<String>>();
		try (BufferedReader br = new BufferedReader(new FileReader("../share/truth_data.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(",",5);
		    	String docno = splitLine[0];
		    	String tid = splitLine[1];
		    	String index = topicDomains.get(tid);
		    	if (!docIndex.containsKey(index)){
		    		docIndex.put(index, new HashSet<String>());
		    	}
		    	docIndex.get(index).add(docno);
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
		
		
		FileWriter fw = new FileWriter("docnos_lengths.txt");
	    BufferedWriter bw = new BufferedWriter(fw);
	    out = new PrintWriter(bw);

		
		for (String index : docIndex.keySet()) {
			try {
				IndexReader reader = DirectoryReader.open(FSDirectory.open( new File("../etc/indices/" + index).toPath()) );
				IndexSearcher searcher = new IndexSearcher(reader);
				int n = reader.numDocs();
				Set<String> docnos = docIndex.get(index);
				for (int i = 0; i < n; i++) {
					Document doc = searcher.doc(i);
					String docno = doc.get("docno");
					if (docnos.contains(docno)){
						Terms termContent = reader.getTermVector(i, "content");
						int length = 0;
						TermsEnum iterator;
						BytesRef term;
						if (termContent!=null) {
							iterator = termContent.iterator();
							term =  iterator.next();
							while (term != null) {
								length += iterator.totalTermFreq();
								term = iterator.next();
							}
						}
						out.println(docno + " " + length);
					}

				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

}
