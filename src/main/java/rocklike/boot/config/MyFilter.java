package rocklike.boot.config;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author 홍순풍(rocklike@gmail.com)
 */
public class MyFilter implements Filter {
	protected Logger logger = LoggerFactory.getLogger(this.getClass());

	private FilterConfig filterConfig;
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException,
			ServletException {
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponse response = (HttpServletResponse) resp;

		String uri = request.getRequestURI();
		String remoteAddr = request.getRemoteAddr();
		logger.info("=== [url:{}] [remoteAddr:{}] ", uri, remoteAddr);

		chain.doFilter(request, response);
	}


	@Override
	public void destroy() {

	}

}
