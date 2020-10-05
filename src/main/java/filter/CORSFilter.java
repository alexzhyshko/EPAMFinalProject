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


public class CORSFilter implements Filter {

	private FilterConfig config = null;
	private boolean active = false;

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		this.config = fConfig;
		String act = config.getInitParameter("active");
		if (act != null)
			active = act.equalsIgnoreCase("true");
	}

	private void setAccessControlHeaders(HttpServletResponse resp) {
		resp.setHeader("Access-Control-Allow-Origin", "http://localhost:4200");
		resp.setHeader("Access-Control-Allow-Methods", "*");
		resp.setHeader("Access-Control-Allow-Headers", "*");
		resp.setHeader("Access-Control-Expose-Headers", "*");
		resp.setHeader("Access-Control-Allow-Credentials", "true");
		resp.setCharacterEncoding("UTF-8");
		resp.setHeader("Charset", "UTF-8");
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		if (active) {
			HttpServletRequest httpReq = (HttpServletRequest) request;
			HttpServletResponse httpRes = (HttpServletResponse) response;
			setAccessControlHeaders(httpRes);
			if ("OPTIONS".equalsIgnoreCase(httpReq.getMethod())) {
				httpRes.setStatus(HttpServletResponse.SC_OK);
				return;
			}
		}
		chain.doFilter(request, response);
	}

}
