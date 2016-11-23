package br.ufmg.dcc.latin.indexing;


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;

import org.apache.commons.io.IOUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.kohlschutter.boilerpipe.BoilerpipeExtractor;
import com.kohlschutter.boilerpipe.BoilerpipeProcessingException;
import com.kohlschutter.boilerpipe.extractors.CommonExtractors;

public class ExtractingUtils {
	

    
    static Parser parser = new AutoDetectParser();
    
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
    public static String extractArticle(String content) throws BoilerpipeProcessingException  {
        BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
        try {
        	 return extractor.getText(content);
		} catch (ArrayIndexOutOfBoundsException e) {
			return extractor.getText(flattenToAscii(content));
		}
       
    }
    
    public static String extractDefault(String content) throws BoilerpipeProcessingException{
        BoilerpipeExtractor extractor = CommonExtractors.DEFAULT_EXTRACTOR;
        try {
       	 return extractor.getText(content);
		} catch (ArrayIndexOutOfBoundsException e) {
			return extractor.getText(flattenToAscii(content));
		}
    }
    
    public static String extractKeep(String content) throws BoilerpipeProcessingException{
        BoilerpipeExtractor extractor = CommonExtractors.KEEP_EVERYTHING_EXTRACTOR;
        try {
       	 return extractor.getText(content);
		} catch (ArrayIndexOutOfBoundsException e) {
			return extractor.getText(flattenToAscii(content));
		}
    }
    
   
    public static String extractJsoup(String content){
    	Document html = null;
    	try {
    		html = Jsoup.parse(content);
		} catch (Exception e) {
			html = Jsoup.parse(flattenToAscii(content));
		}
    	
    	return html.text();
    }
    
    public static String extractTika(String content) throws IOException, SAXException, TikaException {
    	Metadata metadata = new Metadata();
        ContentHandler handler = new BodyContentHandler(-1);
        ParseContext context = new ParseContext();
    	String utfHtmlContent = new String(content.getBytes(),"UTF-8");
    	InputStream htmlStream = new ByteArrayInputStream(utfHtmlContent.getBytes());
    	parser.parse(htmlStream, handler, metadata, context);
    	return handler.toString();
    }
}
