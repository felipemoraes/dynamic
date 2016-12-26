package br.ufmg.dcc.latin.reranker;

import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;

public class Baseline extends InteractiveReranker {

	public Baseline(FeedbackModeling feedbackModeling) {
		super(feedbackModeling);
	}

	@Override
	protected double score(int docid) {
		return relevance[docid];
	}

	@Override
	protected void update(int docid) {
	}

}
