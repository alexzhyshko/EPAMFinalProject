package main.java.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.dto.request.LoginRequest;
import main.java.dto.request.LogoutRequest;
import main.java.dto.request.RegisterRequest;
import main.java.dto.response.LoginResponse;
import main.java.dto.response.RefreshTokenResponse;
import main.java.exception.AuthException;
import main.java.exception.CouldNotParseBodyException;
import main.java.exception.DuplicateUserException;
import main.java.exception.LogoutException;
import main.java.service.AuthService;
import main.java.service.LocalizationService;
import main.java.utils.HttpUtils;

@Component
@RestController
public class AuthController {

	@Inject
	AuthService authService;

	@Inject
	LocalizationService localizator;

	@Mapping(route = "/login", requestType = RequestType.POST)
	public void getLoginRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		try {
			LoginRequest requestObj = HttpUtils.parseBody(req, LoginRequest.class)
					.orElseThrow(() -> new CouldNotParseBodyException("Could not parse body"));
			LoginResponse response = this.authService.login(requestObj, userLocale);
			HttpUtils.setResponseBody(resp, response, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
		} catch (Exception e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_BAD_REQUEST);
		}
	}

	@Mapping(route = "/register", requestType = RequestType.POST)
	public void getRegisterRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		try {
			RegisterRequest requestObj = HttpUtils.parseBody(req, RegisterRequest.class)
					.orElseThrow(() -> new CouldNotParseBodyException("Could not parse body"));
			this.authService.register(requestObj, userLocale);
			HttpUtils.setResponseBody(resp, "Registered", ContentType.TEXT_PLAIN, HttpStatus.SC_OK);
		} catch (CouldNotParseBodyException e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_BAD_REQUEST);
		} catch (DuplicateUserException e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_CONFLICT);
		}
	}

	@Mapping(route = "/refreshToken:arg:arg", requestType = RequestType.GET)
	public void getRefreshTokenRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		try {
			String refreshToken = HttpUtils.parseInputParameter(req, "refreshToken", userLocale, String.class);
			String token = HttpUtils.parseInputParameter(req, "token", userLocale, String.class);
			RefreshTokenResponse response = this.authService.refreshToken(token, refreshToken, userLocale);
			HttpUtils.setResponseBody(resp, response, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
		} catch (AuthException e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_FORBIDDEN);
		} catch (IllegalArgumentException e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_BAD_REQUEST);
		}
	}

	@Mapping(route = "/logout", requestType = RequestType.POST)
	public void getLogoutRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		try {
			LogoutRequest request = HttpUtils.parseBody(req, LogoutRequest.class)
					.orElseThrow(() -> new CouldNotParseBodyException("Could not parse body"));
			this.authService.logout(request);
			resp.setStatus(HttpStatus.SC_OK);
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "ok")).flush();
		} catch (LogoutException e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_BAD_REQUEST);
		}
	}

}
