package br.ufmg.dcc.latin.indexing;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;

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


public class CluewebIndexing {
	
	private static FieldType ft = new FieldType();
	
	public static List<String> readFileListFromDirectory(String directoryPath) throws IOException{
		List<String> fileList = new ArrayList<String>();

		Files.walk(Paths.get(directoryPath)).forEach(filePath -> {
			    if (Files.isRegularFile(filePath)) {
			    	if (filePath.toString().endsWith(".warc.gz") && filePath.toString().contains("wb")) {
			    		fileList.add(filePath.toString());
			    	}
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
            iwc.setRAMBufferSizeMB(32000);
            iwc.setMaxBufferedDocs(1000000);
            iwc.setRAMPerThreadHardLimitMB(2047);
            
            System.out.println(iwc.getRAMPerThreadHardLimitMB());
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


    public static String flattenToAscii(String string) {
        char[] out = new char[string.length()];
        string = Normalizer.normalize(string, Normalizer.Form.NFD);
        int j = 0;
        for (int i = 0, n = string.length(); i < n; ++i) {
            char c = string.charAt(i);
            if (c <= '\u007F') out[j++] = c;
        }
        return new String(out);
    }
    
    
    
    public static List<Document> createDocumentsFromFile(String file) throws IOException{
    	System.out.println("Parsing file " + file);
    	
    	List<Document> docs = new ArrayList<Document>();
		
		InputStream fp = new FileInputStream(file);
		
		if (file.endsWith(".gz")) {
			fp = new GZIPInputStream(fp);
		}
		
    	WarcReader reader = WarcReaderFactory.getReader(fp);
		WarcRecord record;
		
		while ( (record = reader.getNextRecord()) != null ) {

			
            String content = "";
            String title = "";
            
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
        		Parser parser = new AutoDetectParser();
            	Metadata metadata = new Metadata();
                ContentHandler handler = new BodyContentHandler(-1);
                ParseContext context = new ParseContext();
            	String utfHtmlContent = new String(content.getBytes(),"UTF-8");
            	InputStream htmlStream = new ByteArrayInputStream(utfHtmlContent.getBytes());
            	parser.parse(htmlStream, handler, metadata, context);
            	title = metadata.get("title");
				content = handler.toString();
			} catch (SAXException | TikaException  e) {
				//e.printStackTrace();
		    	org.jsoup.nodes.Document html = null;
		    	try {
		    		html = Jsoup.parse(content,"", org.jsoup.parser.Parser.xmlParser());
		    		html = Jsoup.parse(html.html());
				} catch (Exception e_in) {
					html = Jsoup.parse(flattenToAscii(content));
				}
		    	if (html != null) {
		    		content = html.text();
		    		title = html.title();
		    	}
				
			} 
        	
        	if (title == null) {
        		title = "";
        	}
        	if (content == null) {
        		content = "";
        	}
            
            Document doc = new Document();
            Field docnoField = new StringField("docno", key, Field.Store.YES);
            doc.add(docnoField);
            Field urlField = new StringField("url", url, Field.Store.YES);
            doc.add(urlField);

            Field contentField = new Field("content", content,ft);
            doc.add(contentField);
            
            Field titleField = new Field("title", title,ft);
            doc.add(titleField);
            
            
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

	public static int indexedDocCounter = 0;


	public static void main(String[] args) {
		
		String collectionPath = "/Users/felipemoraes/Developer/DiskB/";
        String indexPath = "/Users/felipemoraes/DiskB_index";
        
        
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
        	
        } catch(Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
       //     System.exit(1);
        }
        
        int counter = 0;
        
        try {
        	List<String> files = readFileListFromDirectory(collectionPath);
        	
        	IndexWriter writer =  createWriter(indexPath);
        	System.out.println("Start indexing...");
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
