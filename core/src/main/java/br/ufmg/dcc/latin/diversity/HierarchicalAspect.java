package br.ufmg.dcc.latin.diversity;

public class HierarchicalAspect implements Aspect {

	private float value;
	
	@Override
	public float getValue() {
		return value;
	}
	
	@Override
	public void setValue(float value) {
		this.value = value;
		
	}
}
