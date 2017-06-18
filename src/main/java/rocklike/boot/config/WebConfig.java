package rocklike.boot.config;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.Environment;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter  {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	@Override
	public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
		logger.info("==");
		if(converters!=null){
			for(HttpMessageConverter converter : converters){
				if(converter instanceof MappingJackson2HttpMessageConverter){
					Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
					builder.serializationInclusion(JsonInclude.Include.NON_NULL);
					builder.failOnEmptyBeans(false);
					builder.failOnUnknownProperties(false);
					builder.featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
					builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT);
					MappingJackson2HttpMessageConverter c = (MappingJackson2HttpMessageConverter)converter;
					builder.configure(c.getObjectMapper());
				}
			}
		}
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**").addResourceLocations("/resources/");
	}

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public ExceptionHandlerExceptionResolver getExceptionHandlerExceptionResolver(){
    	return new ExceptionHandlerExceptionResolver();
    }

    @Bean
    public FilterRegistrationBean myFilter() {
    	MyFilter filter = new MyFilter();
        FilterRegistrationBean reg = new FilterRegistrationBean(filter);
        reg.addUrlPatterns("/*");
        return reg;
    }

    @Bean
    public FilterRegistrationBean characterEncodingFilter() {
		CharacterEncodingFilter filter = new CharacterEncodingFilter();
		filter.setEncoding("UTF8");
    	FilterRegistrationBean reg = new FilterRegistrationBean(filter);
    	reg.addUrlPatterns("/*");
    	return reg;
    }


	@Bean
    public InternalResourceViewResolver jspViewResolver() {
        InternalResourceViewResolver bean = new InternalResourceViewResolver();
        bean.setPrefix("/WEB-INF/jsp/");
        bean.setSuffix(".jsp");
        return bean;
    }


	@Override
	public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
		System.out.println("=== configureMessageConverters");
		Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
		builder.serializationInclusion(JsonInclude.Include.NON_NULL);
		builder.failOnEmptyBeans(false);
		builder.failOnUnknownProperties(false);
		builder.featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
		builder.featuresToEnable(SerializationFeature.INDENT_OUTPUT);
		ObjectMapper mapper = builder.build();
		converters.add(new MappingJackson2HttpMessageConverter(mapper));
	}

}
