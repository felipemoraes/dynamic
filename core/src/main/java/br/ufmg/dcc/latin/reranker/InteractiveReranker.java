package br.ufmg.dcc.latin.reranker;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.distribution.BinomialDistribution;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.BooleanSelectedSet;
import br.ufmg.dcc.latin.querying.ResultSet;

public abstract class InteractiveReranker implements Reranker {
	
	public double[] relevance;
	public int[] docids;
	public String[] docnos;
	
	protected String query;
	protected String indexName;
	
	public BooleanSelectedSet selected;
	
	protected FeedbackModeling feedbackModeling;

	protected abstract double score(int docid);
	
	protected abstract void update(int docid);
	
	public int depth;
	
	private boolean stop;
	
	private String stopCondition;
	
	private int offTopicCount;
	
	public int stoppedAt;
	private List<Integer> windowedOffTopicCount;
	
	
	public void update(Feedback[] feedback){
		int windowCount = 0;
		
		for (int i = 0; i < feedback.length; i++) {
			if (feedback[i] == null){
				continue;
			}
			
			if (!feedback[i].onTopic){
				offTopicCount++;
				windowCount++;
			}
		}
		windowedOffTopicCount.add(windowCount);
		if (stopCondition.equals("S2")) {
			if (offTopicCount >= 10){
				if (stop ==false) {
					stoppedAt++;
				}
				stop = true;
			} else {
				stoppedAt++;
			}
		} else if (stopCondition.equals("S3")){
			int count = 0;
			int j = windowedOffTopicCount.size() - 1;
			for (int i = 0; i < 2; i++) {
				count += windowedOffTopicCount.get(j);
				j--;
				if (j<0) {
					break;
				}
			}
			if (count >= 10 ){
				if (stop == false) {
					stoppedAt++;
				}
				stop = true;
			} else {
				stoppedAt++;
			}
		} else if (stopCondition.equals("S1")){
			if (windowedOffTopicCount.size() > 10) {
				stop = true;
			} else {
				stoppedAt++;
			}
		} else if (stopCondition.equals("S0")){
			stop = false;
			stoppedAt++;
		}

		
		
	}
	
	public InteractiveReranker(FeedbackModeling feedbackModeling){
		this.feedbackModeling = feedbackModeling;
		stop = false;
		this.stopCondition = "S0";
		offTopicCount = 0;
		stoppedAt = 0;
		windowedOffTopicCount = new ArrayList<Integer>();
	}
	
	
	
	@Override
	public ResultSet get(){
		
		ResultSet result = new ResultSet(5);
		
		if (stop) {
			return result;
		}
		
		int depth = Math.min(relevance.length, this.depth+selected.size());	
		// greedily diversify the top documents
		int k = 0;
		while(k < 5){
			double maxScore = Double.NEGATIVE_INFINITY;
			int maxRank = -1;
			
			// for each unselected document
			for (int i = 0; i < depth; ++i ){ 
				// skip already selected documents
				if (selected.has(i)){
					continue;
				}
				double score = score(i);
				if (score > maxScore) {
					maxScore = score;
					maxRank = i;
				}
			}
			
			// update the score of the selected document
			if (maxRank < 0) {
				break;
			}
			
			result.scores[k] = maxScore;
			result.docids[k] = docids[maxRank];
			result.docnos[k] = docnos[maxRank];
			result.index[k] = maxRank;
			// mark as selected
			selected.put(maxRank);
			update(maxRank);
			k++;
		}
		if (k == 0) {
			result.docnos = null;
			return result;
		}
		
		for (int i = k; i < 5; i++) {
			result.docnos[k] = null;
		}
		
		return result;
	}


	public void updateDropAspect(Feedback[] feedback, double frac) {
		
	}
	
	public void start(ResultSet resultSet, double[] params){
		depth = 1000;
		docids = resultSet.docids;
		relevance = resultSet.scores;
		docnos = resultSet.docnos;
		selected = new BooleanSelectedSet(docnos.length);
	}

	public void updateDropFeedback(Feedback[] feedbacks, double frac) {
		
	}
	
	
	protected Feedback[] removeFeedback(Feedback[] feedback, double frac) {
		BinomialDistribution binom = new BinomialDistribution(1,frac);
		List<Feedback> feedbacks = new ArrayList<Feedback>();
		for (int i = 0; i < feedback.length; i++) {
			if (binom.sample() == 1) {
				feedbacks.add(feedback[i]);
			}
		}
		int n = feedbacks.size();
		Feedback[] feedbacksRemoved = new Feedback[n];
		for (int i = 0; i < feedbacksRemoved.length; i++) {
			feedbacksRemoved[i] = feedbacks.get(i);
		}
		return feedbacksRemoved;
	}

	public String getStopCondition() {
		return stopCondition;
	}

	public void setStopCondition(String stopCondition) {
		this.stopCondition = stopCondition;
	}

}
