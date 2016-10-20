package br.ufmg.dcc.latin.models.fsdm;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.MapDeserializer;

public class FrequenciesDeserializer extends JsonDeserializer<HashMap<String, HashMap<String, Integer>>>{

	@Override
	public HashMap<String, HashMap<String, Integer>> deserialize(JsonParser jp, DeserializationContext context)
			throws IOException, JsonProcessingException {
		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		
		//System.out.println(jp.getCurrentToken());
		//JsonNode node = jp.getCodec().readTree(jp);
		//System.out.println(">>>>"+node.fields().next());
		
		HashMap<String, HashMap<String, Integer>> intoValue = new HashMap<String, HashMap<String, Integer>>();
		return intoValue;
	}
	
}
