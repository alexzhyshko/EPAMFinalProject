package main.java.controller;

import java.util.UUID;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.component.Component;
import application.context.annotation.component.RestController;
import application.context.annotation.inject.Inject;
import application.context.annotation.mapping.Mapping;
import application.context.annotation.mapping.RequestHeader;
import application.context.annotation.mapping.RequestParameter;
import application.context.annotation.mapping.RequestType;
import application.entity.ResponseEntity;
import main.java.entity.User;
import main.java.service.LocalizationService;
import main.java.service.UserService;

@Component
@RestController
public class UserController {

	@Inject
	private UserService userService;

	@Inject
	private LocalizationService localizator;
	
	private static final String USER_LOCALE_HEADER_NAME="User_Locale";

	@Mapping(route = "/user/getById:arg", requestType = RequestType.GET)
	public ResponseEntity<Object> getUserById(@RequestParameter("userId") UUID userId,
			@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		try {
			User user = this.userService.getUserById(userId);
			user.setPassword(null);
			return new ResponseEntity<>(user, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (NullPointerException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_NOT_FOUND, ContentType.TEXT_PLAIN);
		}
		
	}

	@Mapping(route = "/user/getByUsername:arg", requestType = RequestType.GET)
	public ResponseEntity<Object> getUserByUsername(@RequestParameter("username") String username,
			@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		if (username == null) {
			return new ResponseEntity<>(localizator.getPropertyByLocale(userLocale, "incorrectUsername"), HttpStatus.SC_FORBIDDEN, ContentType.TEXT_PLAIN);
		}
		User user = userService.getUserByUsernameOrNull(username);
		if (user == null) {
			return new ResponseEntity<>(localizator.getPropertyByLocale(userLocale, "userNotFound"), HttpStatus.SC_NOT_FOUND, ContentType.TEXT_PLAIN);
		}
		user.setPassword(null);
		return new ResponseEntity<>(user, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
	}

}
