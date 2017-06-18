package rocklike.boot.biz;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
@Controller
public class WelcomeController {

	@Value("${websocket.port}")
	private String websocketPort;

	@RequestMapping("/")
	public String index(Map<String, Object> model, HttpServletRequest request) {
		String requestURL = request.getRequestURL().toString();
		// http://localhost:10101/
		int pos = 0;
		if(requestURL.startsWith("http://")){
			pos = "http://".length();
		}else{
			pos = "https://".length();
		}
		int pos2 = requestURL.indexOf(":", pos);
		if(pos2<0){
			pos2 = requestURL.length();
		}
		int pos3 = requestURL.indexOf("/", pos);
		if(pos3<0){
			pos3 = requestURL.length();
		}
		pos2 = Math.min(pos2, pos3);
		String host = requestURL.substring(pos, pos2);
		model.put("host", host);
		model.put("websocketPort", websocketPort);

		return "index";
	}

	@RequestMapping("/msgPopup")
	public String msgPopup(Map<String, Object> model) {
		return "msgPopup";
	}

	@RequestMapping("/sess")
	public String makeSession(HttpServletRequest request, HttpServletResponse response, Map<String,String> model){
	    Cookie cookie = new Cookie("name", "honghong");
	    response.addCookie(cookie);
	    cookie = new Cookie("age", "18");
	    response.addCookie(cookie);
	    model.put("message", "cookie set");
//		request.getSession().setAttribute("name", "hong");
		return "welcome";
	}

}