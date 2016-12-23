package org.apache.lucene.search.similarities;

import java.io.IOException;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.util.BytesRef;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Bayesian smoothing using Dirichlet priors. From Chengxiang Zhai and John Lafferty. 2001. A study of smoothing methods
 * for language models applied to Ad Hoc information retrieval. In Proceedings of the 24th annual international ACM
 * SIGIR conference on Research and development in information retrieval (SIGIR '01). ACM, New York, NY, USA, 334-342.
 * <p>
 * The formula as defined the paper assigns a negative score to documents that contain the term, but with fewer
 * occurrences than predicted by the collection language model. The Lucene implementation returns {@code 0} for such
 * documents.
 * </p>
 * 
 * @lucene.experimental
 */
public class LMDirichlet extends LMDirichletSimilarity {

  public LMDirichlet(float mu) {
	  super(mu);
  }
  
  @Override
  public SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
    if (stats instanceof MultiSimilarity.MultiStats) {
      // a multi term query (e.g. phrase). return the summation, 
      // scoring almost as if it were boolean query
      SimWeight subStats[] = ((MultiSimilarity.MultiStats) stats).subStats;
      SimScorer subScorers[] = new SimScorer[subStats.length];
      for (int i = 0; i < subScorers.length; i++) {
        BasicStats basicstats = (BasicStats) subStats[i];
        subScorers[i] = new DirichletLMSimScorer(basicstats, context.reader().getNormValues(basicstats.field));
      }
      return new MultiSimilarity.MultiSimScorer(subScorers);
    } else {
      BasicStats basicstats = (BasicStats) stats;
      return new DirichletLMSimScorer(basicstats, context.reader().getNormValues(basicstats.field));
    }
  }
  
  @Override
  public long computeNorm(FieldInvertState state) {
    return state.getLength();
  }
  
  private class DirichletLMSimScorer extends SimScorer {
    private final BasicStats stats;
    private final NumericDocValues norms;
    
    DirichletLMSimScorer(BasicStats stats, NumericDocValues norms) throws IOException {
      this.stats = stats;
      this.norms = norms;
    }
    
    @Override
    public float score(int doc, float freq) {
      // We have to supply something in case norms are omitted
      return LMDirichlet.this.score(stats, freq, norms.get(doc));
    }
    
    @Override
    public Explanation explain(int doc, Explanation freq) {
    	System.out.println(((LMStats)stats).getCollectionProbability());
      return LMDirichlet.this.explain(stats, doc, freq, norms.get(doc));
    }
    
    
    
    @Override
    public float computeSlopFactor(int distance) {
      return 1.0f / (distance + 1);
    }
    
    @Override
    public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
      return 1f;
    }
  }
  
}
