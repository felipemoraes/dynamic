package br.ufmg.dcc.latin.utils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DynamicSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.baselineranker.ResultList;


public class RetrievalSystem {
	
	public static QueryParser parser;
	
	public static double[] fieldWeights;
	
	public static DynamicSimilarity similarity;

	private static String currentIndex = null;

	private static IndexSearcher searcher;
	
	private static Directory passageDir;
	
	private static IndexReader passageReader;
	
	
	public static QueryParser buildQueryParser(){
		if (parser != null) {
			return parser;
		}
		Analyzer analyzer = new EnglishAnalyzer();
		Map<String,Float> boosts = new HashMap<String,Float>();
		SharedCache.fieldWeights = fieldWeights;
		boosts.put("title", (float) fieldWeights[0]);
		boosts.put("content", (float) fieldWeights[1]);
		parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer, boosts);
		return parser;
		
	}
	
	
	
	public static ResultList search(String query, String index) {
		
		int size = 1000;
		
		setIndexSearcher(index);
		
		ScoreDoc[] hits = null;
		
		TopDocs results = null;
		
		Query q = null;
		try {
			q = parser.parse(QueryParser.escape(query));
			results = searcher.search(q, size);
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		}
	
		
        hits = results.scoreDocs;
        int n = Math.min(size, hits.length);
        
    	int[] docids = new int[n];
    	String[] docnos = new String[n];
   
    	double[] scores = new double[n];
    	
    	
        for(int i=0; i< n; i++){
			try {
				 Document doc = searcher.doc(hits[i].doc);
	             docnos[i] = doc.get("docno");
	             scores[i] = Double.parseDouble(Float.toString(hits[i].score));
	             docids[i] = hits[i].doc;
	             
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
       
        
		ResultList resultList = new ResultList(5);
		
		for (int i = 0; i < 5 ; i++) {
			resultList.docids[i] = docids[i];
			resultList.scores[i] = scores[i];
			resultList.docnos[i] = docnos[i];
		}
		
		SharedCache.docids = docids;
		SharedCache.scores = scores;
		SharedCache.docnos = docnos;
		
		initInMemoryIndex(index);
		
		return resultList;
	}

	
	private static void initInMemoryIndex(String index) {
		if (currentIndex != null) {
			if (currentIndex.equals(index)){
				return;
			}
		} else {
			initPassageIndex();
		}
		
		try {
			
			Terms termsPassage = MultiFields.getTerms(passageReader, "passage");
			
			Terms termsContent = MultiFields.getTerms(searcher.getIndexReader(), "content");
			SharedCache.vocab = new InMemoryVocabulary[2];
			SharedCache.termStats = new InMemoryTermStats[2];
			SharedCache.directedIndex = new InMemoryDirectedIndex[2];
			
			SharedCache.vocab[0] = new InMemoryVocabulary(termsPassage);
			SharedCache.termStats[0] = new InMemoryTermStats(termsContent, SharedCache.vocab[0]);
			
			
			Terms termsTitle = MultiFields.getTerms(searcher.getIndexReader(), "title");
			SharedCache.vocab[1] = new InMemoryVocabulary(termsPassage);
			SharedCache.termStats[1] = new InMemoryTermStats(termsTitle, SharedCache.vocab[1]);

			IndexReader indexReader = searcher.getIndexReader();
			SharedCache.directedIndex[0] = new InMemoryDirectedIndex(SharedCache.vocab[0].size(), indexReader.getDocCount("content")
					, indexReader.getSumTotalTermFreq("content"), indexReader.getSumDocFreq("content"));
			SharedCache.directedIndex[1] = new InMemoryDirectedIndex(SharedCache.vocab[1].size(), indexReader.getDocCount("title")
				, indexReader.getSumTotalTermFreq("title"), indexReader.getSumDocFreq("title"));
			
			
			for (int i = 0; i < SharedCache.docids.length; i++) {
				try {
					SharedCache.directedIndex[0].docVecs[i] = processDocVec(SharedCache.vocab[0], 
							searcher.getIndexReader().getTermVector(SharedCache.docids[i], "content"));
					SharedCache.directedIndex[1].docVecs[i] = processDocVec(SharedCache.vocab[1], 
							searcher.getIndexReader().getTermVector(SharedCache.docids[i], "title"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			for (int i = 0; i < SharedCache.docids.length; i++) {
				int[] terms = SharedCache.directedIndex[0].docVecs[i].vec.keys();
				for (int j = 0; j < terms.length; j++) {
					SharedCache.directedIndex[0].invertedIndex[terms[j]].add(i);
				}
				terms = SharedCache.directedIndex[1].docVecs[i].vec.keys();
				for (int j = 0; j < terms.length; j++) {
					SharedCache.directedIndex[1].invertedIndex[terms[j]].add(i);
					
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}
	
	private static DocVec processDocVec(InMemoryVocabulary vocab, Terms terms) {
		
		DocVec docVec = new DocVec();
		int doclen = 0;
		if (terms == null){
			return docVec;
		}
		try {
			TermsEnum iterator = terms.iterator();
			BytesRef term =  iterator.next();
			while (term != null) {
				String t = term.utf8ToString();
				int termId = vocab.getId(t);
				if (termId != -1){
					docVec.add(termId,(int) iterator.totalTermFreq());
				} 
				doclen += iterator.totalTermFreq();
				term = iterator.next();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		docVec.docLen = doclen;
		return docVec;
	}


	private static void setIndexSearcher(String index) {
		
		IndexReader reader;
		searcher = null;
		
		if (currentIndex!= null) {
			if (currentIndex.equals(index)) {
				return;
			}
		}
		
		try {
			reader = DirectoryReader.open(FSDirectory.open( new File("../etc/indices/" + index).toPath()) );
			searcher = new IndexSearcher(reader);
			searcher.setSimilarity((Similarity) similarity);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}


	public static void initPassageIndex() {
	
		try {
			if (passageDir == null){
				passageDir = new RAMDirectory(FSDirectory.open(new File("../etc/indices/passages").toPath()), IOContext.DEFAULT);
				passageReader = DirectoryReader.open(passageDir);
			}
			
		} catch (IOException e) {
			e.printStackTrace();

		}
	}
	
	public static String getPassage(int passageId){
		try {
			return passageReader.document(passageId).get("passage");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


}
