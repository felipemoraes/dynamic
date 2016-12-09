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
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class PassageIndexing {
	
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
		String passagesFile = "../share/passages.txt";
		
		int counter = 0;
        ft.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
        ft.setStoreTermVectors( true );
        ft.setStoreTermVectorOffsets( true );
        ft.setStoreTermVectorPayloads( true );
        ft.setStoreTermVectorPositions( true );
        ft.setTokenized( true );
        ft.setStored(true);
		
		IndexWriter writer =  createWriter("../etc/indices/passages");
		
		try (BufferedReader br = new BufferedReader(new FileReader(passagesFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split(",",2);
				
				
				String passage = splitLine[1];
				
		        Document doc = new Document();
		        Field queryField = new Field("passage", passage,ft);
		        doc.add(queryField);
		        
		        writer.addDocument(doc);
		        counter++;
		        if (counter % 1000 == 0){
		        	System.out.println("Indexed " +  counter +  " passages.");
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
