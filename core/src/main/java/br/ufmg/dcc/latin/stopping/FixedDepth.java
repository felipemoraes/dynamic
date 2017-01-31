package br.ufmg.dcc.latin.stopping;

import br.ufmg.dcc.latin.user.FeedbackList;

public class FixedDepth extends Stopping {
	private int countDepth; 
	
	public FixedDepth(){
		countDepth = 1;
	}
	
	@Override
	public boolean stop(FeedbackList feedbackList) {
		
		if (countDepth < 10){
			countDepth++;
			return false;
		}
		
		return true;
	}

}
