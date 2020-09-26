package main.java.controller;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.auth.AuthContext;
import main.java.dto.request.LoginRequest;
import main.java.dto.request.LogoutRequest;
import main.java.dto.request.RegisterRequest;
import main.java.dto.response.LoginResponse;
import main.java.dto.response.RefreshTokenResponse;
import main.java.entity.User;
import main.java.service.AuthService;
import main.java.service.HashService;
import main.java.service.TokenService;
import main.java.service.UserService;

@Component
@RestController
public class AuthController {

	private Gson gson = new Gson();

	@Inject
	AuthService authService;

	@Inject
	TokenService tokenService;

	@Inject
	UserService userService;

	@Inject
	private HashService hashService;
	
	
	@Mapping(route = "/login", requestType = RequestType.POST)
	public void getLoginRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		LoginRequest requestObj = gson.fromJson(body, LoginRequest.class);
		String refreshToken = UUID.randomUUID().toString();
		User user = User.builder().username(requestObj.username).password(hashService.hashStringMD5(requestObj.password))
				.refreshToken(refreshToken).build();
		boolean login = authService.login(user);
		if (!login) {
			resp.setStatus(HttpStatus.SC_NOT_FOUND);
			resp.getWriter().append("Incorrect username or password").flush();
			return;
		}
		String jwt = tokenService.generateJwt(user);
		LoginResponse response = new LoginResponse();
		response.refreshToken = refreshToken;
		response.token = jwt;
		response.username = user.getUsername();
		user.setToken(jwt);
		userService.updateToken(user, jwt);
		userService.updateRefreshToken(user, refreshToken);
		String jsonResponse = gson.toJson(response);
		AuthContext.authorize(user);
		resp.setContentType("text/json");
		resp.getWriter().append(jsonResponse).flush();
		resp.setStatus(HttpStatus.SC_OK);
	}

	@Mapping(route = "/register", requestType = RequestType.POST)
	public void getRegisterRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		RegisterRequest requestObj = gson.fromJson(body, RegisterRequest.class);
		User user = User.builder().name(requestObj.name).username(requestObj.username).surname(requestObj.surname)
				.password(hashService.hashStringMD5(requestObj.password)).build();
		boolean created = userService.tryCreateUser(user);
		if(created) {
			resp.setStatus(HttpStatus.SC_CREATED);
			resp.getWriter().append("Registration successful").flush();
			return;
		}
		resp.setStatus(HttpStatus.SC_CONFLICT);
		resp.getWriter().append("User with this username already exists").flush();
	}

	@Mapping(route = "/refreshToken:arg:arg", requestType = RequestType.GET)
	public void getRefreshTokenRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String refreshToken = req.getParameter("refreshToken");
		String token = req.getParameter("token");
		if (refreshToken == null) {
			resp.setContentType("text/plain");
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append("Refresh token = null").flush();
			return;
		}
		User user = AuthContext.getUserByToken(token);
		if (user == null) {
			resp.setContentType("text/plain");
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append("Not authorized").flush();
			return;
		}
		String refreshTokenOfUser = user.getRefreshToken();
		if (refreshTokenOfUser == null || !refreshTokenOfUser.equals(refreshToken)) {
			resp.setContentType("text/plain");
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append("Refresh token of user null or not equals").flush();
			return;
		}
		String newToken = tokenService.generateJwt(user);
		user.setToken(newToken);
		userService.updateToken(user, newToken);
		RefreshTokenResponse response = new RefreshTokenResponse();
		response.refreshToken = refreshTokenOfUser;
		response.token = newToken;
		String jsonResponse = gson.toJson(response);
		resp.setContentType("text/json");
		resp.setStatus(HttpStatus.SC_OK);
		resp.getWriter().append(jsonResponse).flush();
	}
	
	@Mapping(route = "/logout", requestType = RequestType.POST)
	public void getLogoutRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		LogoutRequest request = gson.fromJson(body, LogoutRequest.class);
		String token = request.token;
		User user = AuthContext.getUserByToken(token);
		if (user == null) {
			resp.setStatus(400);
			return;
		}
		AuthContext.deauthorize(user);
		userService.deleteToken(token);
		resp.setStatus(HttpStatus.SC_OK);
		resp.getWriter().append("OK").flush();
	}

}
