package rocklike.boot.netty.common;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class JustTest {

	public static void main(String[] args) throws Exception {
		JustTest m = new JustTest();
		m.test();

	}


	public Map<String, String> m = new HashMap();


	void test() throws JsonGenerationException, JsonMappingException, IOException{
		m.put("1", "11");
		m.put("2", "22");
		m.put("3", "33");
		ObjectMapper om = new ObjectMapper();
		om.configure(SerializationFeature.INDENT_OUTPUT, true);
		om.writeValue(System.out, this);
	}

	void test2(){

	}

	static class Clz{
		public static Clz c(){
			return new Clz();
		}
	}
}
