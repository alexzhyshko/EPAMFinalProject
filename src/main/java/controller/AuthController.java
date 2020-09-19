package main.java.controller;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.auth.AuthContext;
import main.java.dto.User;
import main.java.dto.request.LoginRequest;
import main.java.dto.request.LogoutRequest;
import main.java.dto.request.RegisterRequest;
import main.java.dto.response.LoginResponse;
import main.java.dto.response.RefreshTokenResponse;
import main.java.service.AuthService;
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

	@Mapping(route = "/login", requestType = RequestType.POST)
	public void getLoginRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		LoginRequest requestObj = gson.fromJson(body, LoginRequest.class);
		String refreshToken = UUID.randomUUID().toString();
		User user = User.builder().username(requestObj.username).password(requestObj.password)
				.refreshToken(refreshToken).build();
		boolean login = authService.login(user);
		if (!login) {
			resp.setStatus(302);
			resp.getWriter().append("Register first").flush();
			return;
		}
		String jwt = tokenService.generateJwt(user);
		LoginResponse response = new LoginResponse();
		response.refreshToken = refreshToken;
		response.token = jwt;
		response.username = user.getUsername();
		user.setToken(jwt);
		userService.updateToken(user, jwt);
		String jsonResponse = gson.toJson(response);
		AuthContext.authorize(user);
		resp.setContentType("text/json");
		resp.getWriter().append(jsonResponse).flush();
		resp.setStatus(200);
	}

	@Mapping(route = "/register", requestType = RequestType.POST)
	public void getRegisterRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		RegisterRequest requestObj = gson.fromJson(body, RegisterRequest.class);
		User user = User.builder().name(requestObj.name).username(requestObj.username).surname(requestObj.surname)
				.password(requestObj.password).build();
		boolean created = userService.tryCreateUser(user);
		if(created) {
			resp.getWriter().append("Created").flush();
			resp.setStatus(201);
			return;
		}
		resp.getWriter().append("Conflict").flush();
		resp.setStatus(409);
	}

	@Mapping(route = "/refreshToken:arg", requestType = RequestType.GET)
	public void getRefreshTokenRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String refreshToken = req.getParameter("refreshToken");
		if (refreshToken == null) {
			resp.setStatus(403);
			return;
		}
		User user = AuthContext.getUserByRefreshToken(refreshToken);
		if (user == null) {
			resp.setStatus(403);
			return;
		}
		String refreshTokenOfUser = user.getRefreshToken();
		if (refreshTokenOfUser == null || !refreshTokenOfUser.equals(refreshToken)) {
			resp.setStatus(403);
			return;
		}
		String newRefresh = UUID.randomUUID().toString();
		String newToken = tokenService.generateJwt(user);
		user.setRefreshToken(newRefresh);
		user.setToken(newToken);
		userService.updateToken(user, newToken);
		RefreshTokenResponse response = new RefreshTokenResponse();
		response.refreshToken = newRefresh;
		response.token = newToken;
		String jsonResponse = gson.toJson(response);
		resp.setContentType("text/json");
		resp.getWriter().append(jsonResponse).flush();
		resp.setStatus(200);
	}
	
	@Mapping(route = "/logout", requestType = RequestType.POST)
	public void getLogoutRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		LogoutRequest request = gson.fromJson(body, LogoutRequest.class);
		String token = request.token;
		String refreshToken = request.refreshToken;
		User user = AuthContext.getUserByRefreshToken(refreshToken);
		if (user == null) {
			resp.setStatus(400);
			return;
		}
		AuthContext.deauthorize(user);
		userService.deleteToken(token);
		resp.getWriter().append("OK").flush();
		resp.setStatus(200);
	}

}
