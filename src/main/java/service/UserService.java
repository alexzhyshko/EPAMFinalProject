package main.java.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import main.java.entity.Role;
import main.java.entity.User;
import main.java.repository.UserRepository;

@Component
public class UserService {

	@Inject("UserRepositoryImpl")
	public UserRepository userRepository;

	public void updateToken(User user, String newToken) {
		userRepository.updateTokenByUsername(user.getUsername(), newToken);
	}

	public void updateRefreshToken(User user, String refreshToken) {
		userRepository.updateRefreshTokenByUsername(user.getUsername(), refreshToken);
	}

	public void deleteToken(String token) {
		userRepository.deleteToken(token);
	}

	public boolean tryCreateUser(User user) {
		user.setRole(Role.USER);
		return userRepository.tryCreateUser(user);
	}

	public Optional<User> getUserByUsername(String username) {
		return userRepository.getUserByUsername(username);
	}

	public boolean userExists(User user) {
		return !getUserByUsername(user.getUsername()).isEmpty();
	}

	public boolean userExistsWithPasswordEquals(User user) {
		try {
			User dbUser = userRepository.getUserByUsernameAndPassword(user.getUsername(), user.getPassword())
					.orElseThrow(() -> new NullPointerException("No user exists with these credentials"));
			return !(dbUser == null || !dbUser.getPassword().equals(user.getPassword()));
		} catch (NullPointerException e) {
			return false;
		}
	}

	public User getUserByToken(String token) {
		return userRepository.getUserByToken(token).orElseThrow(()->new NullPointerException("No user found by token"));
	}

	public User getUserById(UUID id) {
		return userRepository.getUserById(id).orElseThrow(()->new NullPointerException("No user found by id"));
	}

	public List<User> getAllUsers() {
		return userRepository.getAllUsers();
	}

}
