package rocklike.boot.netty.common;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class JacksonHelper {
	private JacksonHelper() { }
	private static final ObjectMapper mapper = new ObjectMapper();

	public static String toJsonStr(Object obj){
		String jsonStr=null;
		try {
			jsonStr = mapper.writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonStr;
	}

	public static String toJsonStrFormatted(Object obj){
		String jsonStr=null;
		try {
			jsonStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return jsonStr;
	}


	public static <T> T toObj(String str, Class<T> clz){
		try {
			return mapper.readValue(str, clz);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
