package rocklike.boot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.support.SpringBootServletInitializer;

/**
 * main 시작 class
 * @author 홍순풍(rocklike@gmail.com)
 */
@SpringBootApplication
public class SpringBootWebApplication extends SpringBootServletInitializer {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Override
	protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
		logger.info("== springboot start..");
		return application.sources(SpringBootWebApplication.class);
	}

	public static void main(String[] args) throws Exception {
		SpringApplication.run(SpringBootWebApplication.class, args);
	}



}