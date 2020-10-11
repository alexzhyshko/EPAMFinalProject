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

import application.context.ApplicationContext;
import application.context.annotation.component.Component;
import io.jsonwebtoken.ExpiredJwtException;
import main.java.auth.AuthContext;
import main.java.service.TokenService;

@Component
public class AuthenticationFilter implements Filter {

	private FilterConfig config = null;
	private boolean active = false;

	private TokenService tokenService = (TokenService) ApplicationContext.getSingletonComponent(TokenService.class);

	@Override
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		String act = config.getInitParameter("active");
		if (act != null)
			active = act.equalsIgnoreCase("true");
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest httpReq = (HttpServletRequest) request;
		HttpServletResponse httpRes = (HttpServletResponse) response;
		if (!active) {
			// if filter is deactivated
			chain.doFilter(httpReq, httpRes);
			return;
		}

		// check if path matches filtration exceptions
		
		String path = httpReq.getRequestURI().substring(5);
		if (DefaultFilterChecks.checkFilterExceptions(path)) {
			chain.doFilter(httpReq, httpRes);
			return;
		}

		// check if the auth header exists
		String authTokenHeader = httpReq.getHeader("Authorization");
		if (!checkAuthTokenHeaderValidity(authTokenHeader)) {
			httpRes.setStatus(403);
			httpRes.getWriter().append("Forbidden").flush();
			return;
		}

		// get JWT from headers and check user authorization
		String jwt = authTokenHeader.substring(7);
		
		if (!AuthContext.isAuthorized(jwt)) {
			httpRes.setStatus(403);
			httpRes.getWriter().append("Forbidden").flush();
			return;
		}
		boolean tokenValid = false;
		try {
			// try to validate token
			tokenValid = tokenService.validateToken(jwt);
		} catch (ExpiredJwtException expired) {
			tokenValid = false;
		}
		if (tokenValid) {
			// if token is valid
			chain.doFilter(httpReq, httpRes);
			return;
		}

		// else throw 403 Forbidden
		httpRes.setStatus(403);
		httpRes.getWriter().append("Forbidden").flush();
	}
	
	private boolean checkAuthTokenHeaderValidity(String header) {
		return header != null && !header.isEmpty();
	}

	@Override
	public void destroy() {
		config = null;
	}
}
