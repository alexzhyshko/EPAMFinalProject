package test.java.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import main.java.entity.User;
import main.java.repository.UserRepository;

public class UserRepositoryMock implements UserRepository{

	
	private Map<UUID, User> users = new HashMap<>();
	
	@Override
	public void updateTokenByUsername(String username, String newToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateRefreshTokenByUsername(String username, String refreshToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void deleteToken(String newToken) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean tryCreateUser(User user) {
		users.put(user.getId(), user);
		return true;
	}

	@Override
	public Optional<User> getUserById(UUID id) {
		return Optional.of(users.get(id));
	}

	@Override
	public Optional<User> getUserByUsername(String username) {
		return users.values().stream().filter(user->user.getUsername().equals(username)).findFirst();
	}

	@Override
	public Optional<User> getUserByUsernameAndPassword(String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<User> getUserByToken(String token) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<User> getAllUsers() {
		// TODO Auto-generated method stub
		return null;
	}

}
