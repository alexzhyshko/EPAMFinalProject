package main.java.service;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.Role;
import main.java.dto.User;
import main.java.repository.UserRepository;

@Component
public class UserService {

	@Inject
	private UserRepository userRepository;
	
	public void updateToken(User user, String newToken) {
		userRepository.updateTokenByUsername(user.getUsername(), newToken);
	}
	
	public void updateRefreshToken(User user, String newRefreshToken) {
		userRepository.updateRefreshTokenByUsername(user.getUsername(), newRefreshToken);
	}
	
	public void deleteToken(User user, String newToken) {
		userRepository.deleteTokenByUsername(user.getUsername(), newToken);
	}
	
	public void deleteRefreshToken(User user, String newRefreshToken) {
		userRepository.deleteRefreshTokenByUsername(user.getUsername(), newRefreshToken);
	}
	
	public boolean createUser(User user) {
		user.setRole(Role.USER);
		return userRepository.createUser(user);
	}
	
	public User getUserByUsername(String username) {
		return userRepository.getUserByUsername(username);
	}

	public boolean userExists(User user) {
		return getUserByUsername(user.getUsername())!=null;
	}

	public boolean userExistsWithPasswordEquals(User user) {
		User dbUser = userRepository.getUserByUsernameAndPassword(user.getUsername(), user.getPassword());
		if(dbUser==null || !dbUser.getPassword().equals(user.getPassword())) {
			return false;
		}
		return true;
	}
	
}
