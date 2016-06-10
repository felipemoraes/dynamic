/**
 * 
 */
package br.ufmg.dcc.latin.searcher.utils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Felipe Moraes
 *
 */
public class DDFeedbackSignals extends FeedbackSignals {
	private Set<DDFeedback> ddFeedbackSet;
	/**
	 * @param ddFeedbackSet
	 */
	public DDFeedbackSignals() {
		this.ddFeedbackSet = new HashSet<DDFeedback>();
	}
	public Set<DDFeedback> getDdFeedbackSet() {
		return ddFeedbackSet;
	}
	public void setDdFeedbackSet(Set<DDFeedback> ddFeedbackSet) {
		this.ddFeedbackSet = ddFeedbackSet;
	}
	
	public void addDdFeedback(DDFeedback dDFeedback){
		this.ddFeedbackSet.add(dDFeedback);
	}

}
