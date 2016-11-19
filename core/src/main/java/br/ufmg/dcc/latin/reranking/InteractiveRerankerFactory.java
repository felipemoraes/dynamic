package br.ufmg.dcc.latin.reranking;

public class InteractiveRerankerFactory {
	
	public static InteractiveReranker getInstance(String className){
		InteractiveReranker reranker = null;

		if (className.equals("xQuAD")){
			reranker = new xQuAD();
		} else if (className.equals("MMR")){
			reranker = new MMR();
		} else if (className.equals("PM2")){
			reranker = new PM2();
		} else if (className.equals("xMMR")){
			reranker = new xMMR();
		} else if (className.equals("xQuAD1")){
			reranker = new xQuAD1();
		} else if (className.equals("xQuAD2")){
			reranker = new xQuAD2();
		} else if (className.equals("xQuAD3")){
			reranker = new xQuAD3();
		} else if (className.equals("LM")) {
			reranker = new Baseline(className);
		} else if (className.equals("DPH")) {
			reranker = new Baseline(className);
		} else if (className.equals("BM25")) {
			reranker = new Baseline(className);
		}else if (className.equals("Cube")) {
			reranker = new Cube();
		}else if (className.equals("xQuAD*")) {
			reranker = new xQuADStar();
		}
		
		return reranker;
	}
	
}
