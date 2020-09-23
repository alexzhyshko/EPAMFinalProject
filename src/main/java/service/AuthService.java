package main.java.service;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.auth.AuthContext;
import main.java.entity.User;

@Component
public class AuthService {

	@Inject
	private UserService userService;

	
	public boolean logout(User user) {
		if(!userService.userExists(user)) {
			return false;
		}
		AuthContext.deauthorize(user);
		return true;
	}
	
	public boolean login(User user) {
		if(!userService.userExistsWithPasswordEquals(user)) {
			return false;
		}
		AuthContext.authorize(user);
		return true;
	}
	
}
