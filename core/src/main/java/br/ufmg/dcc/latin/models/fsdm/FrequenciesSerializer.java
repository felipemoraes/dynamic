package br.ufmg.dcc.latin.models.fsdm;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class FrequenciesSerializer extends JsonSerializer<HashMap<String, HashMap<String, Integer>>>{

	@Override
	public void serialize(HashMap<String, HashMap<String, Integer>> arg0, JsonGenerator arg1, SerializerProvider arg2)
			throws IOException, JsonProcessingException {
		System.out.println("AKI");
		
	}
	
}
