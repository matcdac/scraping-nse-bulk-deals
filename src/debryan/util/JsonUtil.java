package debryan.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

//import lombok.extern.slf4j.Slf4j;

//@Slf4j
public class JsonUtil<T> {

	private ObjectMapper objectMapper;

	public JsonUtil() {
		objectMapper = new ObjectMapper();
		objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		objectMapper.configure(SerializationConfig.Feature.WRITE_NULL_PROPERTIES, false);
	}

	public String convertObjectToJson(T object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			//log.error("convertObjectToJson -> " + object.getClass() + " -> " + object.toString(), e);
			System.out.println("convertObjectToJson -> " + object.getClass() + " -> " + object.toString());
		}
		return null;
	}

	public T convertJsonToObject(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			//log.error("convertJsonToObject -> " + clazz.getName() + " -> " + json, e);
			System.out.println("convertJsonToObject -> " + clazz.getName() + " -> " + json);
		}
		return null;
	}

}
