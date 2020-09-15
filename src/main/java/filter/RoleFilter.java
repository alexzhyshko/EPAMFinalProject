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
import application.context.annotation.Component;
import main.java.auth.AuthContext;
import main.java.dto.Role;
import main.java.dto.User;
import main.java.service.TokenService;
import main.java.service.UserService;

@Component
public class RoleFilter implements Filter {

	private FilterConfig config = null;
	private boolean active = false;

	private TokenService tokenService = (TokenService) ApplicationContext.getSingletonComponent(TokenService.class);

	private UserService userService = (UserService) ApplicationContext.getSingletonComponent(UserService.class);

	@Override
	public void init(FilterConfig fConfig) throws ServletException {
		this.config = fConfig;
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
			chain.doFilter(request, response);
			return;
		}

		// check if path matches filtration exceptions
		String path = httpReq.getRequestURI().substring(5);
		if (DefaultFilterChecks.checkFilterExceptions(path)) {
			chain.doFilter(request, response);
			return;
		}

		// check if the auth header exists
		String authTokenHeader = httpReq.getHeader("Authorization");
		if (authTokenHeader == null || authTokenHeader.isEmpty()) {
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
		// get user using JWT
		String username = tokenService.getUsernameFromJwt(jwt);
		User user = userService.getUserByUsernameOrNull(username);
		Role userRole = user.getRole();

		// check if it is admin and grants all access
		if (userRole == Role.ADMIN) {
			chain.doFilter(request, response);
			return;
		}

		// check if user has permission to non-admin resources
		if (!DefaultFilterChecks.checkOnlyAdminResourcesPath(path) && userRole == Role.USER) {
			chain.doFilter(request, response);
			return;
		}

		httpRes.setStatus(403);
		httpRes.getWriter().append("Forbidden").flush();
	}

	@Override
	public void destroy() {
		config = null;
	}

}
