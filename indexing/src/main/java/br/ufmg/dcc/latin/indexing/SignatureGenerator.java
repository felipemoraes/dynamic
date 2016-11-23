package br.ufmg.dcc.latin.indexing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.servlet.SolrRequestParsers;
import org.apache.solr.update.processor.MD5Signature;
import org.apache.solr.update.processor.TextProfileSignature;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.kohlschutter.boilerpipe.BoilerpipeExtractor;
import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.extractors.CommonExtractors;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;




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

   public static List<String> createSignaturesFromFile(String file) throws IOException{
	   

		CBORFactory f = new CBORFactory();
		ObjectMapper mapper = new ObjectMapper(f);
		
        StringBuffer request = new StringBuffer();
        request.append("quantRate=0.01");
		request.append("&minTokenLen=3");
		SolrParams solrParams = SolrRequestParsers.parseQueryString(request
			.toString());
	

    	System.out.println("Parsing file " + file);
    	
    	
    	List<String> docs = new ArrayList<String>();
		
    	InputStream fp = new FileInputStream(file);
		if (file.endsWith(".gz")) {
			fp = new GZIPInputStream(fp);
		}
		
		ObjectReader r = mapper.reader(Map.class);
		MappingIterator<Map> it = r.readValues(fp);
		while (it.hasNextValue()) {
			Map obj = it.nextValue();

		


            String key = (String) obj.get("key");
            
            String url = (String) obj.get("url");
            
            url = normalizeUrl(url);
            
            LinkedHashMap response = (LinkedHashMap) obj.get("response");
            String content = (String) response.get("body");
            Document html = Jsoup.parse(content);
            
    		String mainContent = "";
    		try{
                mainContent = extractText(html.html());//clean text
                mainContent = Jsoup.parse(mainContent).text();//clean any remaining html source code
               
            }catch (Throwable e){
                mainContent = (Jsoup.parse(html.html()).text());
            }
    		
    		

    		TextProfileSignature signature = new TextProfileSignature();
    		
    		signature.init(solrParams);
            
    		signature.add(mainContent);

            
            String s = Hex.encodeHexString( signature.getSignature() );
           if (duplicates.containsKey(s)) {
        	  
        	   System.out.println("Duplicate doc: " + mainContent);
        	   System.out.println(duplicates.get(s));
			} else {
				duplicates.put(s, mainContent);
			}
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
	
	public static void readRelevants(String filename){
		
		try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line;
			while ((line = br.readLine()) != null) {
		    	String[] splitLine = line.split("\t");
		    	System.out.println(line);
		    	System.out.println(splitLine.length);
		    	relevants.add(splitLine[2]);
	
			}
			
			
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	   
	public static HashSet<String> relevants;
	public static Map<String, String> duplicates;
	
	private static int duplicateCounter = 0;
	public static void main(String[] args) {
		
		duplicates = new HashMap<String, String>();
		relevants = new HashSet<>();
		String collectionPath = "/Users/felipemoraes/ebola16_cbor";
        
		
        try {
        	collectionPath = args[0];
        } catch(Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
           // System.exit(1);
        }
        
        
        //readRelevants(args[1]);
        int counter = 0;
        
        try {
        	List<String> files = readFileListFromDirectory(collectionPath);

        	System.out.println("Start generating signatures ... ");
            for (String f : files) {
                System.out.println("About to Parse Files in: " +  f);
                List<String> signatures = createSignaturesFromFile(f);
             //   writeToFile(collectionPath+"_signatures", signatures);
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
