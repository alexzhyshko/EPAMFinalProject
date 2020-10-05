package main.java.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HeaderFilter implements Filter {

	private FilterConfig config = null;
	private boolean active = false;

	public void init(FilterConfig fConfig) throws ServletException {
		this.config = fConfig;
		String act = config.getInitParameter("active");
		if (act != null)
			active = act.equalsIgnoreCase("true");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		if(active) {
			HttpServletRequest httpReq = (HttpServletRequest) request;
			HttpServletResponse httpRes = (HttpServletResponse) response;
			String path = httpReq.getRequestURI().substring(5);
			if (DefaultFilterChecks.checkFilterExceptions(path)) {
				chain.doFilter(httpReq, httpRes);
				return;
			}
			String userLocale = httpReq.getHeader("User_Locale");
			if (userLocale == null) {
				httpRes.getWriter().append("User locale not set").flush();
				httpRes.setStatus(403);
				return;
			}
		}
		chain.doFilter(request, response);
	}


}
