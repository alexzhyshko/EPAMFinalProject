package main.java.controller;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.component.Component;
import application.context.annotation.component.RestController;
import application.context.annotation.inject.Inject;
import application.context.annotation.mapping.Mapping;
import application.context.annotation.mapping.RequestBody;
import application.context.annotation.mapping.RequestHeader;
import application.context.annotation.mapping.RequestParameter;
import application.context.annotation.mapping.RequestType;
import application.entity.ResponseEntity;
import main.java.dto.request.LoginRequest;
import main.java.dto.request.LogoutRequest;
import main.java.dto.request.RegisterRequest;
import main.java.dto.response.LoginResponse;
import main.java.dto.response.RefreshTokenResponse;
import main.java.exception.AuthException;
import main.java.exception.CouldNotParseBodyException;
import main.java.exception.DuplicateLoginException;
import main.java.exception.DuplicateUserException;
import main.java.exception.LogoutException;
import main.java.service.AuthService;
import main.java.service.LocalizationService;

@Component
@RestController
public class AuthController {

	@Inject
	AuthService authService;

	@Inject
	LocalizationService localizator;

	private static final String USER_LOCALE_HEADER_NAME="User_Locale";
	
	@Mapping(route = "/login", requestType = RequestType.POST)
	public ResponseEntity<Object> getLoginRequest(@RequestBody LoginRequest requestObj, @RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		try {
			LoginResponse response = this.authService.login(requestObj, userLocale);
			return new ResponseEntity<>(response, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ContentType.TEXT_PLAIN);
		}
	}

	@Mapping(route = "/register", requestType = RequestType.POST)
	public ResponseEntity<Object> getRegisterRequest(@RequestBody RegisterRequest requestObj, @RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		try {
			this.authService.register(requestObj, userLocale);
			return new ResponseEntity<>("Registered", HttpStatus.SC_OK, ContentType.TEXT_PLAIN);
		} catch (CouldNotParseBodyException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ContentType.TEXT_PLAIN);
		} catch (DuplicateUserException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_CONFLICT, ContentType.TEXT_PLAIN);
		}
	}

	@Mapping(route = "/refreshToken:arg:arg", requestType = RequestType.GET)
	public ResponseEntity<Object> getRefreshTokenRequest(@RequestParameter("token") String token, @RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		try {
			RefreshTokenResponse response = this.authService.refreshToken(token, userLocale);
			return new ResponseEntity<>(response, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (AuthException | DuplicateLoginException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_FORBIDDEN, ContentType.TEXT_PLAIN);
		}
	}

	@Mapping(route = "/signoff", requestType = RequestType.POST)
	public ResponseEntity<Object> getLogoutRequest(@RequestBody LogoutRequest request, @RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		if(userLocale==null) {
			userLocale = "EN";
		}
		try {
			this.authService.logout(request);
			return new ResponseEntity<>(localizator.getPropertyByLocale(userLocale, "ok"), HttpStatus.SC_OK, ContentType.TEXT_PLAIN);
		} catch (LogoutException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ContentType.TEXT_PLAIN);
		}
	}

}
