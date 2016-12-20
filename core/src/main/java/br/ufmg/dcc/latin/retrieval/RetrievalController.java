package br.ufmg.dcc.latin.retrieval;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Rescorer;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.DPH;
import org.apache.lucene.search.similarities.LMDirichlet;
import org.apache.lucene.search.similarities.ReScoreSimilarity;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

import br.ufmg.dcc.latin.cache.RetrievalCache;
import br.ufmg.dcc.latin.index.DocVec;
import br.ufmg.dcc.latin.index.InMemoryDirectedIndex;
import br.ufmg.dcc.latin.index.InMemoryTermStats;
import br.ufmg.dcc.latin.index.InMemoryVocabulary;
import br.ufmg.dcc.latin.querying.ResultSet;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;


public class RetrievalController {
	
	public static QueryParser parser;
	
	public static double[] fiedlWeights;
	
	private static Analyzer analyzer;
	
	public static ReScoreSimilarity similarity;
	
	public static InMemoryDirectedIndex[] directedIndex;
	
	public static InMemoryVocabulary[] vocab;
	public static InMemoryTermStats[] termStats;
	
	public static DocVec[] passageDocs;
	
	private static Directory passageDir;
	private static IndexReader passageReader;
	
	public static TIntSet queryTermsSet;
	
	
	public static void initVocab(String index, String field){
		
		if (vocab == null){
			vocab = new InMemoryVocabulary[2];
		} else {
			return;
		}
		
		try {
			Terms terms = MultiFields.getTerms(getIndexSearcher(index).getIndexReader(), field);
			vocab[0] = vocab[1] = new InMemoryVocabulary(terms);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void initRestricted(String topicId){
		initVocab("passage","passage");
		initTermStats(RetrievalCache.indexName);
		initDocsVec(topicId, RetrievalCache.docids, RetrievalCache.indexName);
	}
	
	public static void init(String topicId){
		initVocab(RetrievalCache.indexName);
		initTermStats(RetrievalCache.indexName);
		initDocsVec(topicId, RetrievalCache.docids, RetrievalCache.indexName);
	}
	
	public static void initVocab(String index){
		
		if (vocab == null){
			vocab = new InMemoryVocabulary[2];
		} else {
			return;
		}
		
		try {

			Terms termsContent = MultiFields.getTerms(getIndexSearcher(index).getIndexReader(), "content");
			vocab[0] = new InMemoryVocabulary(termsContent);
			Terms termsTitle = MultiFields.getTerms(getIndexSearcher(index).getIndexReader(), "title");
			vocab[1] = new InMemoryVocabulary(termsTitle);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void initTermStats(String index){
		if (termStats == null){
			termStats = new InMemoryTermStats[2];
		} else {
			return;
		}
		
		try {
			
			Terms termsContent = MultiFields.getTerms(getIndexSearcher(index).getIndexReader(), "content");
			termStats[0] = new InMemoryTermStats(termsContent, vocab[0]);
			Terms termsTitle = MultiFields.getTerms(getIndexSearcher(index).getIndexReader(), "title");
			termStats[1] = new InMemoryTermStats(termsTitle, vocab[1]);
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	public static void initInMemoryIndex(String indexName){
		
		
		directedIndex = new InMemoryDirectedIndex[2];
		
		IndexReader indexReader = getIndexSearcher(indexName).getIndexReader();
		try {
			directedIndex[0] = new InMemoryDirectedIndex(vocab[0].size(), indexReader.getDocCount("content")
						, indexReader.getSumTotalTermFreq("content"), indexReader.getSumDocFreq("content"));
			directedIndex[1] = new InMemoryDirectedIndex(vocab[1].size(), indexReader.getDocCount("title")
					, indexReader.getSumTotalTermFreq("title"), indexReader.getSumDocFreq("title"));
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
	
	public static void initPassageDocs(){
		if (passageDocs != null){
			return;
		}
		
		int n = passageReader.numDocs();
		passageDocs = new DocVec[n];
		for (int i = 0; i <n; i++) {
			try {
				Terms terms = passageReader.getTermVector(i, "passage");
				DocVec docVec = processDocVec(vocab[0], terms);
				passageDocs[i] = docVec;
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
	}

	public static DocVec getPassageTerms(int passageId){
		initPassageDocs();
		return passageDocs[passageId];
	}
			
	
	public static IndexSearcher getIndexSearcher(String indexName){
		
		if (similarity == null) {
			similarity = new DPH();
		}
		
		IndexReader reader;
		IndexSearcher searcher = null;
		if (indexName == "passage"){
			initPassageIndex();
			searcher = new IndexSearcher(passageReader);
			return searcher;
		}
		if (RetrievalCache.index == null || indexName != RetrievalCache.indexName) {
			try {
				
				reader = DirectoryReader.open(FSDirectory.open( new File("../etc/indices/" + indexName).toPath()) );
				searcher = new IndexSearcher(reader);
				searcher.setSimilarity((Similarity) similarity);
				RetrievalCache.index = searcher;
				RetrievalCache.indexName = indexName;
				return searcher;
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return searcher;
		} else {
			return RetrievalCache.index;
		}
		
		
	}
	
	// TODO
	public static QueryParser getQueryParser(){
		if (parser != null) {
			return parser;
		}
		if (analyzer == null) {
			getAnalyzer();
		}
		Map<String,Float> boosts = new HashMap<String,Float>();
		fiedlWeights = new double[2];
		fiedlWeights[0] =  0.15f;
		fiedlWeights[1] = 0.85f; 
		boosts.put("title", (float) fiedlWeights[0]);
		boosts.put("content", (float) fiedlWeights[1]);
		parser = new MultiFieldQueryParser(new String[]{"title", "content"}, analyzer, boosts);
		return parser;
	}

	
	public static Analyzer getAnalyzer() {
        return analyzer = new EnglishAnalyzer();

	}
	
	
	static void initInvertedIndex(int[] docids){
		
		for (int i = 0; i < docids.length; i++) {
			int[] terms = directedIndex[0].docVecs[i].vec.keys();
			for (int j = 0; j < terms.length; j++) {
				directedIndex[0].invertedIndex[terms[j]].add(i);
			}
			terms = directedIndex[1].docVecs[i].vec.keys();
			for (int j = 0; j < terms.length; j++) {
				directedIndex[1].invertedIndex[terms[j]].add(i);
				
			}
		}
	}
	
	public static void initQueryTerms(String queryTerms){
		queryTermsSet = new TIntHashSet();
		
		List<String> terms = tokenizeText(queryTerms);
		for (String term : terms) {
			int termId = vocab[0].getId(term);
			if (termId != -1){
				queryTermsSet.add(termId);
			}
		}
	}
	
	public static double[] getSimilaritiesRerank(int[] docids, String query){
		
		int n = RetrievalCache.docids.length;
		double[] scores = new double[n];
		Map<Integer,Integer> mapId = new HashMap<Integer, Integer>();
		for (int i = 0; i < scores.length; i++) {
			mapId.put(RetrievalCache.docids[i], i);
		}
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(RetrievalCache.indexName);
		
		BooleanQuery.setMaxClauseCount(1000000);
		QueryParser parser = getQueryParser();
		Query q = null;
		try {
			q = parser.parse(QueryParser.escape(query.toLowerCase()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		
		Rescorer reRankQueryRescorer = new ReRankQueryRescorer(q, 1.0f);
		
	    try {
	    	
			TopDocs rescoredDocs = reRankQueryRescorer
			        .rescore(searcher, RetrievalCache.topDocs, 1000);
			ScoreDoc[] reRankScoreDocs = rescoredDocs.scoreDocs;
			for (int i = 0; i < reRankScoreDocs.length; i++) {
				int ix = mapId.get(reRankScoreDocs[i].doc);
				scores[ix] = reRankScoreDocs[i].score;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	    return scores;
	}
	
	static void initDocsVec(String topicId, int[] docids, String index) {
		
		if (RetrievalCache.directedIndexCache == null) {
			RetrievalCache.directedIndexCache = new HashMap<String, InMemoryDirectedIndex[]>();
		}
		
		if (RetrievalCache.directedIndexCache.containsKey(topicId)) {
			directedIndex = RetrievalCache.directedIndexCache.get(topicId);
			return;
		}
		
		IndexSearcher searcher  = RetrievalController.getIndexSearcher(index);
		
		IndexReader reader = searcher.getIndexReader();

		
		
		initInMemoryIndex(index);
		
			
		for (int i = 0; i < docids.length; i++) {
			try {
				directedIndex[0].docVecs[i] = processDocVec(vocab[0], reader.getTermVector(docids[i], "content"));
				directedIndex[1].docVecs[i] = processDocVec(vocab[1], reader.getTermVector(docids[i], "title"));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		initInvertedIndex(RetrievalCache.docids);
		
		RetrievalCache.directedIndexCache.put(topicId, directedIndex);
		
		
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
	
	public static List<String> tokenizeText(String text){
		List<String> result = new ArrayList<String>();
		try {
		      TokenStream stream  = RetrievalController.getAnalyzer().tokenStream(null, new StringReader(text));
		      stream.reset();
		      
		      while (stream.incrementToken()) {
		    	  BytesRef term = new BytesRef(stream.getAttribute(CharTermAttribute.class).toString());
		    	  result.add(term.utf8ToString());
		      } 
		      stream.close();
		
		} catch (IOException e) {
			      throw new RuntimeException(e);
		}
		return result;
	}
	
	
	
	public static ResultSet search(String topicId, String queryTerms, String index) {

		if (RetrievalCache.resultSetCache == null) {
			RetrievalCache.resultSetCache = new HashMap<String, ResultSet>();
		}
		
		if (RetrievalCache.resultSetCache.containsKey(topicId)){
			ResultSet result = RetrievalCache.resultSetCache.get(topicId);
			RetrievalCache.docids = result.docids;
			RetrievalCache.scores = result.scores;
			RetrievalCache.docnos = result.docnos;
			RetrievalCache.topDocs = result.topDocs;
			
			return result;
		}
		
		
		int size = 1000;
		
		IndexSearcher searcher = getIndexSearcher(index);
		QueryParser parser = getQueryParser();
		
		ScoreDoc[] hits = null;
		
		TopDocs results = null;
		
		Query query = null;
		try {
			query = parser.parse(QueryParser.escape(queryTerms));

			results = searcher.search(query, size);
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
       
        
		ResultSet resultSet = new ResultSet();
		
		resultSet.docids = docids;
		resultSet.scores = scores;
		resultSet.docnos = docnos;
		resultSet.topDocs = results;

		RetrievalCache.docids = docids;
		RetrievalCache.scores = scores;
		RetrievalCache.docnos = docnos;
		RetrievalCache.topDocs = results;
		
		RetrievalCache.resultSetCache.put(topicId, resultSet);
		
		return resultSet;
	}

	public static void setSimilarity(ReScoreSimilarity sim) {
		similarity = sim;
	}

	public static double[] getFiedlWeights() {
		return fiedlWeights;
	}

	public static void setFiedlWeights(double[] fiedlWeights) {
		RetrievalController.fiedlWeights = fiedlWeights;
	}

	public static String getPassage(int passageId) {
		try {
			return passageReader.document(passageId).get("passage");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
