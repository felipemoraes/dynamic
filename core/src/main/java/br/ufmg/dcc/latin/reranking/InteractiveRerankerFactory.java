package br.ufmg.dcc.latin.reranking;

public class InteractiveRerankerFactory {
	
	public static InteractiveReranker getInstance(String className, String aspectMiningClassName){
		InteractiveReranker reranker = null;

		if (className.equals("xQuAD")){
			reranker = new xQuAD(aspectMiningClassName);
		} else if (className.equals("MMR")){
			reranker = new MMR();
		} else if (className.equals("PM2")){
			reranker = new PM2(aspectMiningClassName);
		} else if (className.equals("xMMR")){
			reranker = new xMMR(aspectMiningClassName);
		} else if (className.equals("LM")) {
			reranker = new Baseline(className);
		} else if (className.equals("DPH")) {
			reranker = new Baseline(className);
		} else if (className.equals("BM25")) {
			reranker = new Baseline(className);
		}else if (className.equals("Cube")) {
			reranker = new Cube(aspectMiningClassName);
		} else if (className.equals("RM3")) {
			reranker = new RM3();
		}else if (className.equals("QE")) {
			reranker = new QE();
		}
		
		
		return reranker;
	}
	
}
