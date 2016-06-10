/**
 * 
 */
package br.ufmg.dcc.latin.simulator.dd;

/**
 * @author Felipe Moraes
 *
 */
public class RunTopicPair {
	private String runId;
	private String topicId;
	
	public RunTopicPair(String runId, String topicId){
		this.runId = runId;
		this.topicId = topicId;
	}
	public String getRunId() {
		return runId;
	}
	public void setRunId(String runId) {
		this.runId = runId;
	}
	public String getTopicId() {
		return topicId;
	}
	public void setTopicId(String topicId) {
		this.topicId = topicId;
	}
	
}
