package br.com.tasks.rest;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

public class CORSFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		// TODO Auto-generated method stub
	}

	@Override
	public void doFilter(ServletRequest req, ServletResponse res,
			FilterChain chain) throws IOException, ServletException {
		HttpServletResponse response = (HttpServletResponse) res;
		HttpServletRequest request = (HttpServletRequest) req;
		HttpServletResponseWrapper responseWrapper = new HttpServletResponseWrapper(response);
		HttpServletRequestWrapper requestWrapper = new HttpServletRequestWrapper(request);
		responseWrapper.addHeader("Access-Control-Allow-Origin", "*");
		responseWrapper.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		responseWrapper.addHeader("Access-Control-Max-Age", "3600");
		responseWrapper.addHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
		chain.doFilter(requestWrapper, responseWrapper);
		if (responseWrapper.getHeader("Access-Control-Allow-Origin") == null) {
			responseWrapper.addHeader("Access-Control-Allow-Origin", "*");
		}
		responseWrapper.addHeader("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
		responseWrapper.addHeader("Access-Control-Max-Age", "3600");
		responseWrapper.addHeader("Access-Control-Allow-Headers", "Origin, Accept, X-Requested-With, Content-Type, Access-Control-Request-Method, Access-Control-Request-Headers");
		
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

}
