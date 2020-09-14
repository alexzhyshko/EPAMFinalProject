package main.java.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationFilter implements Filter {
	
	private FilterConfig config = null;
	private boolean active = false;
	

	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		String act = config.getInitParameter("active");
		if (act != null)
			active = act.equalsIgnoreCase("true");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletResponse httpRes = (HttpServletResponse)response;
		if (active) {
			httpRes.setStatus(403);
			httpRes.getWriter().append("Forbidden").flush();
			return;
		}
		chain.doFilter(request, response);
	}

	public void destroy() {
		config = null;
	}
}
