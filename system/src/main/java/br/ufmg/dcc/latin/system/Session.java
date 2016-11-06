package br.ufmg.dcc.latin.system;

import java.util.List;

public interface Session {
	
	public void start(String index, String topicId, String topic);
	public void run();
	public void run(String name, float[] params);

	
	List<float[]> getParameters();
}
