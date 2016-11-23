package br.ufmg.dcc.latin.indexing;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.servlet.SolrRequestParsers;
import org.apache.solr.update.processor.TextProfileSignature;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import de.l3s.boilerpipe.BoilerpipeExtractor;
import de.l3s.boilerpipe.BoilerpipeProcessingException;
import de.l3s.boilerpipe.extractors.ArticleExtractor;
import de.l3s.boilerpipe.extractors.CommonExtractors;
import de.l3s.boilerpipe.extractors.DefaultExtractor;
import de.l3s.boilerpipe.extractors.KeepEverythingExtractor;

public class SignatureGenerator {

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
    
    public static String extractText(String html) throws BoilerpipeProcessingException {
        BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
        return extractor.getText(html);
    }

   public static List<String> createSignaturesFromFile(String file) throws IOException{
	   
        StringBuffer request = new StringBuffer();
        request.append("quantRate=0.01");
		request.append("&minTokenLen=2");
		SolrParams solrParams = SolrRequestParsers.parseQueryString(request
			.toString());
	

    	System.out.println("Parsing file " + file);
    	
    	
    	List<String> docs = new ArrayList<String>();
		
		InputStream fp = new FileInputStream(file);
		
    	WarcReader reader = WarcReaderFactory.getReader(fp);
		WarcRecord record;
		
		while ( (record = reader.getNextRecord()) != null ) {


            String key = "";
            
            try {
            	key = record.getHeader("WARC-TREC-ID").value;
            } catch (Exception e){
            	continue;
            }
            
          //  String url = record.getHeader("WARC-Target-URI").value;
          //  url = normalizeUrl(url);
            

            
            String content = IOUtils.toString(record.getPayloadContent(), "UTF-8"); 
            Document html = Jsoup.parse(content);
    		String mainContent = "";
    		try{

                mainContent = extractText(html.html());//clean text
                mainContent = Jsoup.parse(mainContent).text();//clean any remaining html source code
            }catch (Throwable e){
                mainContent = (Jsoup.parse(html.html()).text());
            }

    		
    		
            TextProfileSignature signaure = new TextProfileSignature();
            signaure.init(solrParams);
            
            signaure.add(mainContent);

            
            String s = Hex.encodeHexString( signaure.getSignature() );
         /*   if (duplicates.containsKey(s)) {
            	System.out.println("Duplicated doc: " + url);
				System.out.println(duplicates.get(s));
				 System.out.println(content);
			} else {
				duplicates.put(s, url);
			}*/
            docs.add(key + " " + s);
          
		}

        return docs;
    }
	   
	   
	public static void writeToFile(String filename, List<String> texts){
		System.out.println("Writing signatures to file:" +  filename);
		try(FileWriter fw = new FileWriter(filename, true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {

			for (int i = 0; i < texts.size(); i++) {
				out.println(texts.get(i));
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	   
	public static Map<String,String> duplicates;
	public static void main(String[] args) {
		
		duplicates = new HashMap<String,String>();
		
		String collectionPath = "/Users/felipemoraes/ebola16";
        
		
        try {
        	collectionPath = args[0];
        } catch(Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
           // System.exit(1);
        }
        
        int counter = 0;
        
        try {
        	List<String> files = readFileListFromDirectory(collectionPath);

        	System.out.println("Start generating signatures ... ");
            for (String f : files) {
                System.out.println("About to Parse Files in: " +  f);
                List<String> signatures = createSignaturesFromFile(f);
                writeToFile(collectionPath+"_signatures", signatures);
                counter++;
                System.out.println("Genenated " + counter + " of " + files.size());
                
            }
        	
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            e.printStackTrace();
           
        }
		
	}

}
