package br.ufmg.dcc.latin.searcher;

import java.net.UnknownHostException;

import br.ufmg.dcc.latin.searcher.similarity.Similarity;

public class AdHocSearcherFactory {
	
	private EbolaAdHocSearcher ebolaAdHocSearcher;
	private LocalPoliticsAdHocSearcher localPoliticsAdHocSearcher;
	private IllicitGoodsAdHocSearcher illicitGoodsAdHocSearcher;
	
	public AdHocSearcherFactory(Similarity similarity) throws UnknownHostException{
		ebolaAdHocSearcher = new EbolaAdHocSearcher("ebola_2015",similarity);
		localPoliticsAdHocSearcher = new LocalPoliticsAdHocSearcher("local_politics_2015",similarity);
		illicitGoodsAdHocSearcher = new IllicitGoodsAdHocSearcher("illicit_goods_2015",similarity);
	}
	
	public Searcher getAdHocSearcher(String adHocSearcherType, Similarity similarity) throws UnknownHostException{
	      if(adHocSearcherType == null){
	          return null;
	       }		
	       if(adHocSearcherType.equalsIgnoreCase("ebola_2015")){
	          return ebolaAdHocSearcher;
	       } 
	       else if(adHocSearcherType.equalsIgnoreCase("ebola_2016")){
		      return ebolaAdHocSearcher;
		   } 
	       else if(adHocSearcherType.equalsIgnoreCase("local_politics")){
	          return localPoliticsAdHocSearcher;
	          
	       } else if(adHocSearcherType.equalsIgnoreCase("illicit_goods")){
	          return illicitGoodsAdHocSearcher;
	       }
	       
	       return null;
	}

}
