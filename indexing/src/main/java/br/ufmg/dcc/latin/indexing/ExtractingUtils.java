package br.ufmg.dcc.latin.indexing;


import java.io.IOException;
import java.io.InputStream;

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
	
	static Metadata metadata = new Metadata();
    static ContentHandler handler = new BodyContentHandler(-1);
    static ParseContext context = new ParseContext();
    
    static Parser parser = new AutoDetectParser();
    
    
    public static String extractArticle(String content) throws BoilerpipeProcessingException  {
    	Document html = Jsoup.parse(content);
        BoilerpipeExtractor extractor = CommonExtractors.ARTICLE_EXTRACTOR;
        return extractor.getText(html.html());
    }
    
    public static String extractDefault(String content) throws BoilerpipeProcessingException{
    	Document html = Jsoup.parse(content);
        BoilerpipeExtractor extractor = CommonExtractors.DEFAULT_EXTRACTOR;
        return extractor.getText(html.html());
    }
    
    public static String extractKeep(String content) throws BoilerpipeProcessingException{
    	Document html = Jsoup.parse(content);
        BoilerpipeExtractor extractor = CommonExtractors.KEEP_EVERYTHING_EXTRACTOR;
        return extractor.getText(html.html());
    }
    
    public static String extractJsoup(String content){
    	Document html = Jsoup.parse(content);
    	return html.text();
    }
    
    public static String extractTika(String content) throws IOException, SAXException, TikaException{
    	Document html = Jsoup.parse(content);
    	InputStream in = IOUtils.toInputStream(html.html(), "UTF-8");
    	parser.parse(in, handler, metadata, context);
    	return handler.toString();
    }
}
