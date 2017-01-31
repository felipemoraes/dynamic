package br.ufmg.dcc.latin.indexing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.utils.InMemoryVocabulary;



public class TermsFeaturesProcessing {
	
	public static void wikipedia(InMemoryVocabulary vocab){
		
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
		        	int termId = vocab.getId(term.utf8ToString());
		        	if (termId != -1){
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

	public static void querylog(InMemoryVocabulary vocab){
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
		        	int termId = vocab.getId(term.utf8ToString());
		        	if (termId != -1){
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
	

	
	public static void main(String[] args) {
		
		Directory passageDir;
		IndexReader passageReader;
		
		try {
			passageDir = new RAMDirectory(FSDirectory.open(new File("../etc/indices/passages").toPath()), IOContext.DEFAULT);
			passageReader = DirectoryReader.open(passageDir);
			
			Terms terms = MultiFields.getTerms(passageReader, "passage");
			InMemoryVocabulary vocab = new InMemoryVocabulary(terms);

				
			querylog(vocab);
			wikipedia(vocab);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}

}
