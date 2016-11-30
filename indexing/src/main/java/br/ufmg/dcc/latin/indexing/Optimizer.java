package br.ufmg.dcc.latin.indexing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Optimizer {

	public static Analyzer createAnalyzer() throws IOException{
        Analyzer Analyzer = new EnglishAnalyzer();
        return Analyzer;
	}
	
	public static void main(String[] args) {
		
		String indexPath = args[0];
		IndexReader reader;
		IndexWriter writer = null;
		int count = 0;
		try {
			Directory dir = FSDirectory.open(Paths.get(indexPath));
		
			IndexWriterConfig iwc = new IndexWriterConfig(createAnalyzer());
			    
	        iwc.setSimilarity(new DPH());
	        iwc.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
	        writer = new IndexWriter(dir, iwc);
	        reader = DirectoryReader.open(dir); 
			Set<String> indexDocNos = new HashSet<String>();
			
            
			for (int i=0; i<reader.maxDoc(); i++) {
			    Document doc = reader.document(i);
			    String docno = doc.get("docno");
			    if (indexDocNos.contains(docno)){
			    	// writer.deleteDocuments(new Term("id",String.format("%d", i)));
			    	count++;
			    } else {
			    	indexDocNos.add(docno);
			    }
			    
			    if ( i % 1000 == 0){
			    	System.out.println("Checked: " + i + " documents, found " + count + " duplicates" );
			    }
			
			}
			System.out.println("Duplicated documents: " + count);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		


	}

}
