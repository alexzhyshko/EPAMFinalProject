package main.java.service;

import java.util.HashMap;

import application.context.annotation.Component;
import main.java.auth.AuthContext;
import main.java.dto.User;

@Component
public class AuthService {

	HashMap<String, User> users = new HashMap<>();
	
	public boolean createUser(User user) {
		if(users.containsKey(user.getUsername())) {
			return false;
		}
		users.put(user.getUsername(), user);
		return true;
	}
	
	public boolean logout(User user) {
		if(!users.containsKey(user.getUsername())) {
			return false;
		}
		AuthContext.deauthorize(user);
		return true;
	}
	
	public boolean login(User user) {
		if(!users.containsKey(user.getUsername())) {
			return false;
		}
		AuthContext.authorize(user);
		return true;
	}
	
	
	
}
