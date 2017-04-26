package br.ufmg.dcc.latin.reranker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.math3.distribution.BinomialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;

import br.ufmg.dcc.latin.feedback.Feedback;
import br.ufmg.dcc.latin.feedback.modeling.FeedbackModeling;
import br.ufmg.dcc.latin.querying.BooleanSelectedSet;
import br.ufmg.dcc.latin.querying.ResultSet;
import br.ufmg.dcc.latin.user.TrecUser;

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
	
	public double noiseStop;
	
	
	public void update(Feedback[] feedback){

		
		for (int i = 0; i < feedback.length; i++) {
			
			if (feedback[i] == null){
				windowedOffTopicCount.add(0);
				continue;
			}
			
			if (!feedback[i].onTopic){
				offTopicCount++;
				windowedOffTopicCount.add(0);
			} else {
				windowedOffTopicCount.add(1);
			}
		}
		
		
		
		if (stopCondition.equals("S2")) {
			if (offTopicCount >= 20){
				if (stop == false) {
					stoppedAt++;
				}
				stop = true;
			} else {
				stoppedAt++;
			}
		} else if (stopCondition.equals("S3")){
			int count = 0;
			for (int i = 0; i < windowedOffTopicCount.size(); i++) {
				if (windowedOffTopicCount.get(i) == 1) {
					count = 0;
				} else {
					count++;
				}
			}
			
			if (count >= 20 ){
				if (stop == false) {
					stoppedAt++;
				}
				stop = true;
			} else {
				stoppedAt++;
			}
			
		} else if (stopCondition.equals("S1")){
			if (windowedOffTopicCount.size()/5 >= 10) {
				stop = true;
			} else {
				stoppedAt++;
			}
		} else if (stopCondition.equals("S0")){
			stop = false;
			stoppedAt++;
			
		} else if (stopCondition.equals("S4")){
			boolean answer = oracleSop();
			if (answer) {
				
				if (stop == false) {
					stop = true;
					stoppedAt++;
				} 
				
			} else {
				stoppedAt++;
			};

		}

	}
	
	
	
	private boolean oracleSop() {
		
		double[] relevances = TrecUser.get(docnos);
		int n = 0;
		for (int i = 0; i < relevances.length; i++) {
			if (selected.has(i)){
				continue;
			}
			if (relevances[i] > 0) {
				n++;
			}
		}
		double rand = ThreadLocalRandom.current().nextDouble(0.0, 1.00000000000000000000000001);
		boolean tellTruth = true;
		if (rand < 1-noiseStop ){
			tellTruth = true; 
		} else {
			tellTruth = false;
		}
		
		if (n > 0) {
			return tellTruth? true : false;
		} else {
			return tellTruth? false : true;
		}
		
		/*double std = noiseStop*n;
		if ( std == 0) {
			std = noiseStop;
		}
		
		NormalDistribution normalDistribution = new NormalDistribution(n, std );
		
		double sample = normalDistribution.sample();
		
		if (sample > 0) {
			return false;
		}
		return true;*/
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
		depth = 500;
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
