package main.java.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import main.java.entity.User;

public interface UserRepository {

	void updateTokenByUsername(String username, String newToken);
	void updateRefreshTokenByUsername(String username, String refreshToken);
	void deleteToken(String newToken);
	boolean tryCreateUser(User user);
	Optional<User> getUserById(UUID id);
	Optional<User> getUserByUsername(String username);
	Optional<User> getUserByUsernameAndPassword(String username, String password);
	Optional<User> getUserByToken(String token);
	List<User> getAllUsers();
	
	
	
}
