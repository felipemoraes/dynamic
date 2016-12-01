package br.ufmg.dcc.latin.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.retrieval.RetrievalController;



public class TermsFeaturesProcessing {
	
	public static void wikipedia(Set<String> terms){
		
		IndexReader reader;
		try(FileWriter fw = new FileWriter("../share/wikipedia_counts.txt");
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {
			try {
				reader = DirectoryReader.open(FSDirectory.open( new File("../etc/indices/wikipedia").toPath()) );
				out.println(reader.getDocCount("title") + " " + reader.getSumDocFreq("title") + " " + reader.getSumTotalTermFreq("title"));
				TermsEnum termsEnum = MultiFields.getTerms(reader, "title").iterator();
				BytesRef term = null;
		        while ((term = termsEnum.next()) != null) {
		        	if (terms.contains(term.utf8ToString())){
		        		out.println(term.utf8ToString() + " " + termsEnum.docFreq() + " " + termsEnum.totalTermFreq());
		        	}
		        	
		        }
		        out.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}

	public static void querylog(Set<String> terms){
		IndexReader reader;
		try(FileWriter fw = new FileWriter("../share/msn_counts.txt");
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {
			try {
				reader = DirectoryReader.open(FSDirectory.open( new File("../etc/indices/msn").toPath()) );
				out.println(reader.getDocCount("query") + " " + reader.getSumDocFreq("query") + " " + reader.getSumTotalTermFreq("query"));
				TermsEnum termsEnum = MultiFields.getTerms(reader, "query").iterator();
				BytesRef term = null;
		        while ((term = termsEnum.next()) != null) {
		        	if (terms.contains(term.utf8ToString())){
		        		out.println(term.utf8ToString() + " " + termsEnum.docFreq() + " " + termsEnum.totalTermFreq());
		        	}
		        	
		        }
		        out.close();
		        
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}

	}
	
	
	public static List<String> tokenizeString(Analyzer analyzer, String str) {
		List<String> result = new ArrayList<String>();
		try {
		      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
		      stream.reset();
	
		      while (stream.incrementToken()) {
		    	  result.add(stream.getAttribute(CharTermAttribute.class).toString());
		      } 
		      
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result;
		
	}
	
	public static void main(String[] args) {
		Set<String> passageTerms = new HashSet<String>();
		Analyzer analyzer = RetrievalController.getAnalyzer();
		
		try (BufferedReader br = new BufferedReader(new FileReader("../share/truth_data.txt"))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split(",",5);
		    	
		    	String passage = splitLine[4];
		    	List<String> terms = tokenizeString(analyzer, passage);
		    	for (String term : terms) {
		    		passageTerms.add(term);
				}
		    			
			}
			
			querylog(passageTerms);
			wikipedia(passageTerms);
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

}
