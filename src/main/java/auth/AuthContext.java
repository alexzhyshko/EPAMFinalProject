package main.java.auth;

import java.util.HashMap;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.User;
import main.java.jwt.JwtParser;

@Component
public class AuthContext {

	private static HashMap<String, User> authorizedUsers = new HashMap<>();
	
	@Inject
	private static JwtParser parser;
	
	public static void authorize(User user) {
		if(authorizedUsers.containsKey(user.getUsername())) {
			return;
		}
		authorizedUsers.put(user.getUsername(), user);
	}
	
	public static boolean isAuthorized(User user) {
		User authorizedInstance = authorizedUsers.get(user.getUsername());
		if(authorizedInstance==null) {
			return false;
		}
		return user.getName().equals(authorizedInstance.getName()) && user.getSurname().equals(authorizedInstance.getSurname()) && user.getUsername().equals(authorizedInstance.getUsername());
	}
	
	public static boolean isAuthorized(String jwt) {
		User authorizedInstance = null;
		for(User user : authorizedUsers.values()) {
			if(jwt.equals(user.getToken())) {
				authorizedInstance = user;
			}
		}
		if(authorizedInstance==null) {
			return false;
		}
		String username = (String)parser.parseClaimsFromJwt(jwt).get("username");
		return authorizedInstance.getUsername().equals(username);
	}
	
	public static void deauthorize(User user) {
		authorizedUsers.remove(user.getUsername());
	}
	
	public static User getUserByRefreshToken(String refreshToken) {
		for(User user : authorizedUsers.values()) {
			if(user.getRefreshToken().equals(refreshToken)) {
				return user;
			}
		}
		return null;
	}
	
}
