package br.ufmg.dcc.latin.rescoring;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.QueryRescorer;

public class ReRankQueryRescorer extends QueryRescorer {
	
    final double reRankWeight;

    public ReRankQueryRescorer(Query reRankQuery, double reRankWeight) {
      super(reRankQuery);
      this.reRankWeight = reRankWeight;
    }

    @Override
    protected float combine(float firstPassScore, boolean secondPassMatches, float secondPassScore) {
      float score = firstPassScore;
      if (secondPassMatches) {
        score += reRankWeight * secondPassScore;
      }
      return score;
    }

}
