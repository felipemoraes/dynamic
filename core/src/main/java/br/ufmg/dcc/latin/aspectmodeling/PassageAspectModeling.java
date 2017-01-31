package br.ufmg.dcc.latin.aspectmodeling;

import java.util.ArrayList;
import java.util.List;

import br.ufmg.dcc.latin.user.Feedback;
import br.ufmg.dcc.latin.user.FeedbackList;
import br.ufmg.dcc.latin.utils.RetrievalSystem;

public class PassageAspectModeling extends AspectModeling {
	
	private List<FeedbackList> feedbackLists;
	private PassageAspectModel model;
	
	public PassageAspectModeling(){
		feedbackLists = new ArrayList<FeedbackList>();
		model = new PassageAspectModel();
		RetrievalSystem.initPassageIndex();
	}

	@Override
	public PassageAspectModel getAspectModel(FeedbackList feedbackList) {
		feedbackLists.add(feedbackList);
		for (Feedback feedback : feedbackList.feedbacks) {
			model.addToAspect(feedback.aspectId, feedback.passageId, feedback.relevance);
			model.addToDocument(feedback.docid, feedback.aspectId,feedback.relevance);
		}
		return model;
	}

}
