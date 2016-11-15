package br.ufmg.dcc.latin.indexing;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

public class Indexer {
	
	public static List<String> readFileListFromDirectory(String directoryPath) throws IOException{
		List<String> fileList = new ArrayList<String>();

		Files.walk(Paths.get(directoryPath)).forEach(filePath -> {
			    if (Files.isRegularFile(filePath)) {
			    	fileList.add(filePath.toString());
			    }
		});
		
        Collections.sort(fileList);
        return fileList;
	}
	
	public static Analyzer createAnalyzer() throws IOException{
        CustomAnalyzer.Builder builder = CustomAnalyzer.builder();
        builder.withTokenizer("standard");
        builder.addTokenFilter("lowercase");
        builder.addTokenFilter("stop");
        builder.addTokenFilter("kstem");
        
  
        Analyzer analyzer = builder.build();
        return analyzer;
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
	
    public static void addDocumentToIndex(Document doc, IndexWriter writer){
        try {
            writer.addDocument(doc);
        } catch (IOException e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }
    
    public static String normalizeUrl(String url){
    	
		try {
			url = URLDecoder.decode(url, "UTF-8");
			URI uri;
			uri = new URI(url);
			url = uri.normalize().toString();
		} catch (URISyntaxException e) {

		} catch (IllegalArgumentException e) {

		} catch (UnsupportedEncodingException e) {

		}
		
		return url;
		
    }
    
    public static List<Document> createDocumentsFromFile(String file) throws IOException{
    	System.out.println("Parsing file " + file);
    	
    	List<Document> docs = new ArrayList<Document>();
		
		InputStream fp = new FileInputStream(file);
		
    	WarcReader reader = WarcReaderFactory.getReader(fp);
		WarcRecord record;
		
		while ( (record = reader.getNextRecord()) != null ) {
	        String title = "";
            String description = "";
            String content = "";
            String key = "";
            try {
            	key = record.getHeader("WARC-TREC-ID").value;
            } catch (Exception e){
            	continue;
            }
            if (ignoredDocuments.contains(key)) {
				continue;
			} 
            
           
            String url = record.getHeader("WARC-Target-URI").value;
            url = normalizeUrl(url);
            
            
        	Metadata metadata = new Metadata();
            ContentHandler handler = new BodyContentHandler(10 * 1024 * 1024);
            ParseContext context = new ParseContext();
            
            Parser parser = new AutoDetectParser();
            
            boolean exception = false;
            
            try {
                content = IOUtils.toString(record.getPayloadContent(), "UTF-8"); 
                InputStream in = IOUtils.toInputStream(content, "UTF-8");
            	parser.parse(in, handler, metadata, context);
                title = metadata.get("title");
                description = metadata.get("description");
                content = handler.toString();
            } catch (TikaException e) {
            	exception = true;
            } catch (SAXException e) {
            	exception = true;
            } catch (IOException e) {
            	exception = true;

			} 
            
            if (exception) {
            	try {
               	org.jsoup.nodes.Document doc = Jsoup.parse(content);
            	content = doc.text();
            	title = doc.title();
            	} catch (Exception e){
            	}
            }
            
            if (title == null){
            	title = "";
            }
            if (description == null) {
            	description = "";
            }
            if (content == null){
            	content = "";
            }
            
            Document doc = new Document();
            Field docnoField = new StringField("docno", key, Field.Store.YES);
            doc.add(docnoField);
            Field urlField = new StringField("url", url, Field.Store.YES);
            doc.add(urlField);
            Field titleField = new TextField("title", title, Field.Store.YES);
            doc.add(titleField);
            Field descriptionField = new TextField("description", description, Field.Store.YES);
            doc.add(descriptionField);
            Field contentField = new TextField("content", content, Field.Store.YES);
            doc.add(contentField);
            docs.add(doc);
          
		}

        return docs;
    }
    
	private static void indexDocumentsFromFile(IndexWriter writer, String file) {
			System.out.println("Indexing file " + file);
			List<Document> docs;
			try {
				docs = createDocumentsFromFile(file);
				for (Document doc : docs) {
					addDocumentToIndex(doc,writer);
					indexedDocCounter++;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		
		
		
	}
	
	public static Set<String> ignoredDocuments = new HashSet<String>();
   
	public static int indexedDocCounter = 0;
	
	public static void loadIgnoredFile(String ignoreFile){
		try (BufferedReader br = new BufferedReader(new FileReader(ignoreFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				ignoredDocuments.add(line.replaceAll("\n", ""));
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		String collectionPath = "/Users/felipemoraes/polar/";
        String indexPath = "/Users/felipemoraes/polar/index";
        String ignoreFilePath = "/Users/felipemoraes/polar_duplicates.txt";
        
        try {
        	collectionPath = args[0];
        	indexPath = args[1];
        	ignoreFilePath = args[2];
        } catch(Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(1);
        }
        
        int counter = 0;
        
        try {
        	List<String> files = readFileListFromDirectory(collectionPath);
        	loadIgnoredFile(ignoreFilePath);
        	IndexWriter writer =  createWriter(indexPath);
        	System.out.println("Start indexing... We will ignore " + ignoredDocuments.size() + " documents.");
            for (String f : files) {
                System.out.println("About to Index Files in: " +  f);
                indexDocumentsFromFile(writer,f);
                counter++;
                System.out.println("Indexed " + counter + " of " + files.size());
                System.out.println("Indexed " + indexedDocCounter + " documents.");
            }
            if (writer != null){
                writer.close();
            }
        	
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            e.printStackTrace();
           
        }

	}



}
