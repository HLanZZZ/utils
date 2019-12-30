import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * @ClassName: JsonUtils
 * @Description: JsonUtils
 * @author: wei.yue
 * @date: Nov 30, 2018 4:32:24 PM
 * @version: V1.2[api.versionCpp]
 */
public class JsonUtils {
	private static ObjectMapper mapper;
	private static ObjectMapper policyMapper;
	private static Logger logger = Logger.getLogger(JsonUtils.class);

	public static void initJson() {
		mapper = new ObjectMapper();
		
	}
	public static void initMapper() {
		policyMapper = new ObjectMapper();
		policyMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
		policyMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
		.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
		policyMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}
	
	

	public static <T> T toSnakeObject(String json, Class<T> clazz) throws IOException {
		if (json == null) {
			return null;
		}
		try {
			if(null == policyMapper)
				initMapper();
			return (T) policyMapper.readValue(json, clazz);
		} catch (Exception e) {
		}
		return null;
	}
	
	public static <T> Object convertJsonToPojo(String string, Class<T> class1) throws JsonParseException, JsonMappingException, IOException {
		JsonXNode node = (JsonXNode) policyMapper.readValue(string, JsonXNode.class);
		ObjectXNode o = (ObjectXNode)node;
		T t = o.convertTo(class1);
		return t;
	}

	/**
	 * 
	 * @Title: transFromJson
	 * @Description: no any policy ,only for json transition.
	 * @param json
	 * @param tr
	 * @return
	 * @return: T
	 */
	public static <T> T transFromJson(String json, TypeReference<T> tr) {
		if (json == null) {
			return null;
		}
		if (mapper == null) {
			initJson();
		}
		try {
			return (T) mapper.readValue(json, tr);
		} catch (Exception e) {

			logger.debug("json error:" + e);
		}
		return null;
	}

	/**
	 * 
	 * @Title: transToJson
	 * @Description: no any policy ,only for json transition.
	 * @param object
	 * @return
	 * @return: String
	 */
	public static <T> String transToJson(T object) {
		if (object == null) {
			return null;
		}
		if (mapper == null) {
			initJson();
		}
		try {
			return mapper.writeValueAsString(object);
		} catch (Exception e) {
		}
		return null;
	}
	/**
	 * 
	 * @Title: transToJson
	 * @Description: return SNAKE_CASE, include NON_NULL
	 * @param object
	 * @return
	 * @return: return a string, if the object is null return ""
	 */
	public static <T> String translateToJson(T object) {
		String result = "";
		if(null == object)
			return result;
		try {
			if(null == policyMapper)
				initMapper();
			result = policyMapper.writeValueAsString(object);
		} catch (Exception e) {
			result = "Failed to translate Object:" + object.toString();
		}
		return result;
	}

}
