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

	public void deleteToken(String token) {
		userRepository.deleteToken(token);
	}

	public boolean tryCreateUser(User user) {
		user.setRole(Role.USER);
		return userRepository.tryCreateUser(user);
	}

	public User getUserByUsernameOrNull(String username) {
		return userRepository.getUserByUsernameOrNull(username);
	}

	public boolean userExists(User user) {
		return getUserByUsernameOrNull(user.getUsername()) != null;
	}

	public boolean userExistsWithPasswordEquals(User user) {
		User dbUser = userRepository.getUserByUsernameAndPassword(user.getUsername(), user.getPassword());
		return !(dbUser == null || !dbUser.getPassword().equals(user.getPassword()));
	}

}
