package main.java.repository;

import java.sql.Connection;

import application.connection.DBConnectionManager;
import application.context.annotation.Inject;
import main.java.dto.User;

public class UserRepository {

	@Inject
	private DBConnectionManager connectionManager;
	
	private Connection getNewConnection() {
		return this.connectionManager.getConnection();
	}
	
	public void updateTokenByUsername(String username, String newToken) {
		String query = "";
		// TODO Auto-generated method stub
		
	}

	public void updateRefreshTokenByUsername(String username, String newRefreshToken) {
		// TODO Auto-generated method stub
		
	}

	public void deleteTokenByUsername(String username, String newToken) {
		// TODO Auto-generated method stub
		
	}

	public void deleteRefreshTokenByUsername(String username, String newRefreshToken) {
		// TODO Auto-generated method stub
		
	}

	public boolean createUser(User user) {
		// TODO Auto-generated method stub
		return false;
	}

	public User getUserByUsername(String username) {
		// TODO Auto-generated method stub
		return null;
	}

	public User getUserByUsernameAndPassword(String username, String password) {
		// TODO Auto-generated method stub
		return null;
	}

}
