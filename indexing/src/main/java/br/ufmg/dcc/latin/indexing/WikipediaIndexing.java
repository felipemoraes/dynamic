package br.ufmg.dcc.latin.indexing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class WikipediaIndexing {

	private static FieldType ft = new FieldType();

	public static Analyzer createAnalyzer() throws IOException{
        Analyzer Analyzer = new EnglishAnalyzer();
        return Analyzer;
	}
	
	public static IndexWriter createWriter(String indexPath){
		IndexWriter writer = null;
		try {
			
			Directory dir = FSDirectory.open(Paths.get(indexPath));
            System.out.println("Indexing to directory '" + indexPath + "'...");

            Analyzer analyzer = createAnalyzer();
            
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
    
            iwc.setSimilarity(new DPH());
            iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            writer = new IndexWriter(dir, iwc);
            
    
            
		} catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
		return writer;
	}
	
	public static void main(String[] args) {
		String wikipediaTitleFile = "/Users/felipemoraes/Developer/dynamic/etc/enwiki-latest-all-titles";
		
		int counter = 0;
        ft.setTokenized( true );
        ft.setStored(true);
		
		IndexWriter writer =  createWriter("/Users/felipemoraes/Developer/dynamic/etc/indices/wikipedia");
		
		try (BufferedReader br = new BufferedReader(new FileReader(wikipediaTitleFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split("\t");
				
				String title = splitLine[1].replaceAll( "_", " ");
				
		        Document doc = new Document();
		        Field titleField = new Field("title", title,ft);
		        doc.add(titleField);
		        
		        writer.addDocument(doc);
		        counter++;
		        if (counter % 100000 == 0){
		        	System.out.println("Indexed " +  counter +  " titles.");
		        }
			}
			writer.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
}
