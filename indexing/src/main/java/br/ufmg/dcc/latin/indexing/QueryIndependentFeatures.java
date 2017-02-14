package br.ufmg.dcc.latin.indexing;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringReader;
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
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.Link;
import org.apache.tika.sax.LinkContentHandler;
import org.apache.tika.sax.TeeContentHandler;
import org.jwat.warc.WarcReader;
import org.jwat.warc.WarcReaderFactory;
import org.jwat.warc.WarcRecord;
import org.xml.sax.SAXException;

import com.kennycason.fleschkincaid.FleschKincaid;



public class QueryIndependentFeatures {
	
	private static IndexReader reader;
	private static IndexSearcher searcher;
	
	private static Map<String,Integer> mapDocnoToDocid;
	private static String[] docidToUrl;
	private static String[] docidToDocno;
	
	private static PrintWriter out;
	
	private static EnglishAnalyzer analyzer = new EnglishAnalyzer();
	private static EnglishAnalyzer analyzerWithoutStop = new EnglishAnalyzer(CharArraySet.EMPTY_SET);
	
	public static int counter;
	
	public static void main(String[] args) throws IOException {
		
		String collectionPath = "/Users/felipemoraes/ebola16/";
        String indexPath = "/Users/felipemoraes/Developer/dynamic/etc/indices/ebola16/";
        String outPath = "/Users/felipemoraes/ebolaOut2016";
      
        try {
        	collectionPath = args[0];
        	indexPath = args[1];
        	outPath = args[2];
        } catch(Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
       //     System.exit(1);
        }
        
        loadIndexInfo(indexPath);
        
	    FileWriter fw = new FileWriter( outPath);
	    BufferedWriter bw = new BufferedWriter(fw);
		out = new PrintWriter(bw);

        
        try {
        	List<String> files = readFileListFromDirectory(collectionPath);
        	int i = 0;
            for (String filename : files) {
                System.out.println("About to Index Files in: " +  filename);
                computeFeaturesFromFile(filename);
                if (counter % 10000 == 0) {
                	System.out.println("Processed " + counter + " documents." );
                }
                System.out.println("Processed " + i + " of " +  files.size() + " files.");
                break;
            }

        	
        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            e.printStackTrace();
           
        }
        out.close();

	}
	
    private static void loadIndexInfo(String indexPath) {
    	try {
			reader = DirectoryReader.open(FSDirectory.open( new File(indexPath).toPath()) );
			searcher = new IndexSearcher(reader);
			int n = reader.numDocs();
			docidToUrl = new String[n];
			docidToDocno = new String[n];
			mapDocnoToDocid = new HashMap<String,Integer>();
			for (int i = 0; i < n; i++) {
				Document doc = searcher.doc(i);
				String docno = doc.get("docno");
				String url = doc.get("url");
				mapDocnoToDocid.put(docno, i);
				docidToUrl[i] = url;
				docidToDocno[i] = docno;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
    
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
	
	private static double[] urlFeatures(int docid){
		double[] features = new double[2];
		String url = docidToUrl[docid];
		String depth[] = url.replace("//", "/").split("/");
		features[0] = depth.length;
		features[1] = url.length();
		return features;
	}
	
	private static double log2(double x){
		return Math.log(x)/Math.log(2);
	}
	
	public static double compress(String data) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length());
		GZIPOutputStream gzip = new GZIPOutputStream(bos);
		double sizeBefore = data.getBytes().length;
		gzip.write(data.getBytes());
		gzip.close();
		byte[] compressed = bos.toByteArray();
		bos.close();
		double sizeAfter = compressed.length;
		if (sizeBefore > 0) {
			return sizeAfter/sizeBefore;
		}
		return 0;
	}

	private static double[] docFeatures(int docid) {
		
		// 0-AVLC, 1-AVLT, 2-CL, 3-TL, 4-TCC, 5-TCL, 6-SFC, 7-SFT, 8-SCC, 9-SCT, 10-CRC, 11-CRT, 12-FK
		double[] features = new double[13];
		double stopWordSetSize = analyzer.getStopwordSet().size();
		try {
			Terms termContent = reader.getTermVector(docid, "content");
			Terms termTitle = reader.getTermVector(docid, "title");
			String content = searcher.doc(docid).get("content");
			String title = searcher.doc(docid).get("title");
			double numOfContentTokens  = tokenCount(content,analyzerWithoutStop);
			double numOfTitleTokens  = tokenCount(title,analyzerWithoutStop);
			features[10] = compress(content);
			features[11] = compress(title);
			FleschKincaid fleshKincaid = new FleschKincaid();
			features[12] = fleshKincaid.calculate(content);
			try {
				TermsEnum iterator = termContent.iterator();
				BytesRef term =  iterator.next();
				while (term != null) {
					String t = term.utf8ToString();
					features[2] += iterator.totalTermFreq();
					features[0] +=  iterator.totalTermFreq()*t.length();
					term = iterator.next();
				}
				
				if (features[2] == 0) {
					features[0] = 0; 
				} else {
					features[0] /= features[2];
				}
				
				iterator = termContent.iterator();
				term =  iterator.next();
				if (features[2] > 0) {
					while (term != null) {
						double prob = iterator.totalTermFreq()/features[2];
						features[4] +=  prob*log2(prob);
						term = iterator.next();
					}
				}
				
				
				
				iterator = termTitle.iterator();
				term =  iterator.next();
				while (term != null) {
					String t = term.utf8ToString();
					features[3] += iterator.totalTermFreq();
					features[1] +=  iterator.totalTermFreq()*t.length();
					
					term = iterator.next();
				}
				if (features[3] == 0) {
					features[1] = 0; 
				} else {
					features[1] /= features[3];
				}
				iterator = termTitle.iterator();
				term =  iterator.next();
				if (features[3] > 0) {
					while (term != null) {
						double prob = iterator.totalTermFreq()/features[3];
						features[5] +=  prob*log2(prob);
						term = iterator.next();
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			features[6] = features[2]/numOfContentTokens;
			features[7] = features[3]/numOfTitleTokens;
			features[8] = features[2]/stopWordSetSize;
			features[9] = features[3]/stopWordSetSize;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		features[4] *=-1;
		features[5] *=-1;
		
		return features;
	}
	
	public static double[] anchorTextFeatures(List<Link> links, double contentSize){
		double[] features = new double[2];
		double AnchorFraction = 0;
		for (Link link : links) {
			AnchorFraction += tokenCount(link.getText(),analyzer);
		}
		
		if (contentSize > 0) {
			AnchorFraction = AnchorFraction/contentSize;
		} else {
			AnchorFraction = 0;
		}
	
		features[0] = AnchorFraction;
		String[] schemes = {"http","https"};
		UrlValidator urlValidator = new UrlValidator(schemes);
		for (Link link : links) {
			features[1] +=   urlValidator.isValid(link.getUri()) && link.getText().length() > 0 ? 1 : 0;
		}
		
		
		return features;
		
	}
	
	public static double tokenCount(String str, Analyzer analyzer) {
		List<String> result = new ArrayList<String>();
		try {
		
		      TokenStream stream  = analyzer.tokenStream(null, new StringReader(str));
		      stream.reset();
	
		      while (stream.incrementToken()) {
		    	  BytesRef term = new BytesRef(stream.getAttribute(CharTermAttribute.class).toString());
		    	  
		    	  result.add(term.utf8ToString());
		      } 
		      
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result.size();
		
	}
	
	

	private static void computeFeaturesFromFile(String filename) throws IOException {
    	System.out.println("Parsing file " + filename);

		InputStream fp = new FileInputStream(filename);
		
    	WarcReader reader = WarcReaderFactory.getReader(fp);
		WarcRecord record;
		
		while ( (record = reader.getNextRecord()) != null ) {

			
            String content = "";
   
            
            String key = "";
            try {
            	key = record.getHeader("WARC-TREC-ID").value;
            } catch (Exception e){
            	continue;
            }
            int docid = -1;
            if (mapDocnoToDocid.containsKey(key)){
            	docid = mapDocnoToDocid.get(key);
            	
            } else {
            	continue;
            }
           

           
            content = IOUtils.toString(record.getPayloadContent()); 
            
        	
        	try {
        		Parser parser = new AutoDetectParser();
            	Metadata metadata = new Metadata();
                
                LinkContentHandler linkHandler = new LinkContentHandler();
                ParseContext context = new ParseContext();
            	String utfHtmlContent = new String(content.getBytes(),"UTF-8");
            	InputStream htmlStream = new ByteArrayInputStream(utfHtmlContent.getBytes());
            	TeeContentHandler teeHandler = new TeeContentHandler(linkHandler);
            	parser.parse(htmlStream, teeHandler, metadata, context);
            	
                double[] urlFeatures = urlFeatures(docid);
                double[] docFeatures = docFeatures(docid);
                double[] anchorFeatures = anchorTextFeatures(linkHandler.getLinks(), docFeatures[2]);
                int k = 1;
                out.print(docid + " ");
                for (int i = 0; i < urlFeatures.length; i++) {
					out.print(k+":"+ urlFeatures[i] + " ");
					k++;
				}
                
                for (int i = 0; i < docFeatures.length; i++) {
 					out.print(k+":"+ docFeatures[i] + " ");
 					k++;
 				}
                
                for (int i = 0; i < anchorFeatures.length; i++) {
 					out.print(k+":"+ anchorFeatures[i] + " ");
 					k++;
 				}
                out.print("\n");

			} catch (SAXException | TikaException  e) {
				continue;
				
			}
        	counter++;
        	
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
	    

}

