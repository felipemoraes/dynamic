package br.ufmg.dcc.latin.diversity;

public class FlatAspect implements Aspect {
	
	private float value;
	
	public FlatAspect(float value){
		this.value = value;
	}

	@Override
	public float getValue() {
		return value;
	}

	@Override
	public void setValue(float value) {
		this.value = value;
		
	}

}
