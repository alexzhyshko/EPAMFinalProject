package main.java.auth;

import java.util.HashMap;
import java.util.Optional;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import io.jsonwebtoken.ExpiredJwtException;
import main.java.entity.User;
import main.java.jwt.JwtParser;
import main.java.service.UserService;

@Component
public class AuthContext {

	private static HashMap<String, User> authorizedUsers = new HashMap<>();
	
	@Inject
	private static JwtParser parser;
	
	@Inject
	private static UserService userService;
	
	public static void authorize(User user) {
		authorizedUsers.put(user.getUsername(), user);
	}
	
	public static boolean isAuthorized(String jwt) {
		User authorizedInstance = null;
		for(User user : authorizedUsers.values()) {
			if(user.getToken()==null && user.getToken().equals(jwt)) {
				authorizedInstance = user;
			}
		}
		boolean needReauth = false;
		if(authorizedInstance==null) {
			needReauth = true;
			authorizedInstance = userService.getUserByToken(jwt);
		}
		if(authorizedInstance==null) {
			return false;
		}
		if(needReauth) {
			authorize(authorizedInstance);
		}
		try {
			String username = (String)parser.parseClaimsFromJwt(jwt).get("username");
			return authorizedInstance.getUsername().equals(username);
		}catch(ExpiredJwtException e) {
			return false;
		}
		
	}
	
	public static void deauthorize(User user) {
		authorizedUsers.remove(user.getUsername());
	}
	
	public static Optional<User> getUserByToken(String token) {
		Optional<User> result = Optional.empty();
		for(User user : authorizedUsers.values()) {
			if(user.getToken()==null && user.getToken().equals(token)) {
				result = Optional.of(user);
			}
		}
		if(result.isEmpty()) {
			result = Optional.of(userService.getUserByToken(token));
		}
		return result;
	}
	
}
