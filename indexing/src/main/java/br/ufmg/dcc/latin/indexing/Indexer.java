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
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.servlet.SolrRequestParsers;
import org.apache.solr.update.processor.MD5Signature;
import org.apache.solr.update.processor.TextProfileSignature;
import org.apache.tika.exception.TikaException;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.xml.sax.SAXException;

import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;


public class Indexer {
	
	private static FieldType ft = new FieldType();
	
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
    
    public static String bytesToHex(byte[] in) {
	    final StringBuilder builder = new StringBuilder();
	    for(byte b : in) {
	        builder.append(String.format("%02x", b));
	    }
	    return builder.toString();
	}
	   

    public static String generateSignature(String content){
    	MD5Signature signature = new MD5Signature();
		
        StringBuffer request = new StringBuffer();
        request.append("quantRate=0.01");
		request.append("&minTokenLen=2");
		SolrParams solrParams = SolrRequestParsers.parseQueryString(request
			.toString());
		
		signature.init(solrParams);
        
		signature.add(content);

        
        String s = bytesToHex( signature.getSignature() );
        return s;
    }

    
    public static List<Document> createDocumentsFromFile(String file) throws IOException{
    	System.out.println("Parsing file " + file);
    	
    	List<Document> docs = new ArrayList<Document>();
		
		InputStream fp = new FileInputStream(file);
		
    	WarcReader reader = WarcReaderFactory.getReader(fp);
		WarcRecord record;
		
		while ( (record = reader.getNextRecord()) != null ) {

			
            String content = "";
            String article_content = "";
            String default_content=  "";
            String keep_content = "";
            String jsoup_content = "";
            String tika_content = "";
            
            String key = "";
            try {
            	key = record.getHeader("WARC-TREC-ID").value;
            } catch (Exception e){
            	continue;
            }

           
            String url = record.getHeader("WARC-Target-URI").value;
            url = normalizeUrl(url);
            
           
            content = IOUtils.toString(record.getPayloadContent()); 
            
            
          
            	try {
					article_content = ExtractingUtils.extractArticle(content);
				} catch (BoilerpipeProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	try {
					default_content = ExtractingUtils.extractDefault(content);
				} catch (BoilerpipeProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	try {
					keep_content = ExtractingUtils.extractKeep(content);
				} catch (BoilerpipeProcessingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            	jsoup_content = ExtractingUtils.extractJsoup(content);
            	try {
					tika_content = ExtractingUtils.extractTika(content);
				} catch (SAXException e) {
					tika_content = jsoup_content;
				} catch (TikaException e) {
					tika_content = jsoup_content;
				}
            	
            
            

            
            Document doc = new Document();
            Field docnoField = new StringField("docno", key, Field.Store.YES);
            doc.add(docnoField);
            Field urlField = new StringField("url", url, Field.Store.YES);
            doc.add(urlField);

            String s = generateSignature(article_content);
            if (relevantDocuments.contains(key) || !articleSignatures.contains(s)){
            	articleSignatures.add(s);
            	Field contentField = new Field("article_content", article_content,ft);
                doc.add(contentField);
            }
            s = generateSignature(default_content);
            if (relevantDocuments.contains(key) || !defaultSignatures.contains(s)){
            	defaultSignatures.add(s);
            	Field contentField = new Field("default_content", default_content,ft);
                doc.add(contentField);
            }
            
            s = generateSignature(keep_content);
            if (relevantDocuments.contains(key) || !keepSignatures.contains(s)){
            	keepSignatures.add(s);
            	Field contentField = new Field("keep_content", keep_content,ft);
                doc.add(contentField);
            }
            
            s = generateSignature(tika_content);
            if (relevantDocuments.contains(key) || !tikaSignatures.contains(s)){
            	tikaSignatures.add(s);
            	Field contentField = new Field("tika_content", tika_content,ft);
                doc.add(contentField);
            }
            
            s = generateSignature(jsoup_content);
            if (relevantDocuments.contains(key) || !jsoupSignatures.contains(s)){
            	jsoupSignatures.add(s);
            	Field contentField = new Field("jsoup_content", jsoup_content,ft);
                doc.add(contentField);
            }
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
	
	public static Set<String> relevantDocuments = new HashSet<String>();
   
	public static int indexedDocCounter = 0;
	
	public static void loadRelevantsFile(String relevantsFile){
		try (BufferedReader br = new BufferedReader(new FileReader(relevantsFile))) {
			String line;
			while ((line = br.readLine()) != null) {
				String[] splitLine = line.split("\t");
				relevantDocuments.add(splitLine[2]);
			}
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	public static Set<String> tikaSignatures;
	public static Set<String> articleSignatures;
	public static Set<String> defaultSignatures;
	public static Set<String> keepSignatures;
	public static Set<String> jsoupSignatures;

	public static void main(String[] args) {
		
		tikaSignatures = new HashSet<String>();
		articleSignatures =  new HashSet<String>();
	    defaultSignatures =  new HashSet<String>();
	    keepSignatures = new HashSet<String>();
	    jsoupSignatures = new HashSet<String>();
	    
		String collectionPath = "/Users/felipemoraes/ebola16/";
        String indexPath = "/Users/felipemoraes/ebola16_index";
        String relevantsFilePath = "/Users/felipemoraes/polar_duplicates.txt";
        
        ft.setIndexOptions( IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS );
        ft.setStoreTermVectors( true );
        ft.setStoreTermVectorOffsets( true );
        ft.setStoreTermVectorPayloads( true );
        ft.setStoreTermVectorPositions( true );
        ft.setTokenized( true );
        ft.setStored(true);
      
        try {
        	collectionPath = args[0];
        	indexPath = args[1];
        	relevantsFilePath = args[2];
        } catch(Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
       //     System.exit(1);
        }
        
        int counter = 0;
        
        try {
        	List<String> files = readFileListFromDirectory(collectionPath);
        	loadRelevantsFile(relevantsFilePath);
        	IndexWriter writer =  createWriter(indexPath);
        	System.out.println("Start indexing... We will ignore " + relevantDocuments.size() + " documents.");
            for (String f : files) {
                System.out.println("About to Index Files in: " +  f);
                indexDocumentsFromFile(writer,f);
                counter++;
                System.out.println("Indexed " + counter + " of " + files.size());
                System.out.println("Indexed " + indexedDocCounter + " documents.");
                System.out.println("Non-duplicate documents with tika: " + tikaSignatures.size());
                System.out.println("Non-duplicate documents with jsoup: " + jsoupSignatures.size());
                System.out.println("Non-duplicate documents with article: " + articleSignatures.size());
                System.out.println("Non-duplicate documents with default: " + defaultSignatures.size());
                System.out.println("Non-duplicate documents with keep: " + keepSignatures.size());
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
