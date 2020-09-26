package main.java.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import application.connection.DBConnectionManager;
import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.entity.Role;
import main.java.entity.User;

@Component
public class UserRepository {

	@Inject
	private DBConnectionManager connectionManager;

	private Connection getNewConnection() {
		return this.connectionManager.getConnection();
	}
	
	private void createTokenForUserByUsername(String username, String newToken) {
		String query = "INSERT INTO Tokens(token, user_id) VALUES(?, (SELECT id FROM `Users` WHERE username=?))";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, newToken);
			ps.setString(2, username);
			int changed = ps.executeUpdate();
			connection.commit();
			if(changed==0) {
				createTokenForUserByUsername(username, newToken);
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void updateTokenByUsername(String username, String newToken) {
		String query = "UPDATE Tokens JOIN Users ON Tokens.user_id = `Users`.id SET Tokens.token=? WHERE `Users`.username=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, newToken);
			ps.setString(2, username);
			int changed = ps.executeUpdate();
			connection.commit();
			if(changed==0) {
				createTokenForUserByUsername(username, newToken);
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateRefreshTokenByUsername(String username, String refreshToken) {
		String query = "UPDATE Tokens JOIN Users ON Tokens.user_id = `Users`.id SET Tokens.refreshToken=? WHERE `Users`.username=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, refreshToken);
			ps.setString(2, username);
			ps.executeUpdate();
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public void deleteToken(String newToken) {
		String query = "DELETE FROM Tokens WHERE token=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, newToken);
			ps.executeUpdate();
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Creates user, is such not already exists
	 * @param user
	 * @return boolean - if user was created
	 */
	public boolean tryCreateUser(User user) {
		String query = "INSERT INTO `Users`(id, username, name, surname, password, role_id) VALUES(?, ?, ?, ?, ?, (SELECT id FROM user_roles WHERE name=?));";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, UUID.randomUUID().toString());
			ps.setString(2, user.getUsername());
			ps.setString(3, user.getName());
			ps.setString(4, user.getSurname());
			ps.setString(5, user.getPassword());
			ps.setString(6, user.getRole().name());
			ps.executeUpdate();
			connection.commit();
			return true;
			
		} catch (SQLIntegrityConstraintViolationException e) {
			//if user exists
			return false;
		}	catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return false;
			}
			e.printStackTrace();
			return false;
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	
	/**
	 * Returns a {@code User}, if such exists in database. If not, returns {@code null}
	 * @param username - used to identify user in database
	 * @return {@code User} or null, if not found
	 */
	public User getUserByUsernameOrNull(String username) {
		String query = "SELECT `Users`.id, `Users`.username, `Users`.name, `Users`.surname, `Users`.rating, `Users`.password, user_roles.name FROM `Users` JOIN user_roles ON `Users`.role_id = user_roles.id WHERE `Users`.username=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, username);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return User.builder()
							.id(UUID.fromString(rs.getString(1)))
							.username(rs.getString(2))
							.name(rs.getString(3))
							.surname(rs.getString(4))
							.rating(rs.getFloat(5))
							.password(rs.getString(6))
							.role(Role.valueOf(rs.getString(7)))
							.build();
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	
	
	public Optional<User> getUserByID(UUID id) {
		String query = "SELECT `Users`.id, `Users`.username, `Users`.name, `Users`.surname, `Users`.rating, `Users`.password, user_roles.name "
				+ "FROM `Users` "
				+ "JOIN user_roles ON `Users`.role_id = user_roles.id "
				+ "WHERE `Users`.id=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, id.toString());
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Optional.of(User.builder()
							.id(UUID.fromString(rs.getString(1)))
							.username(rs.getString(2))
							.name(rs.getString(3))
							.surname(rs.getString(4))
							.rating(rs.getFloat(5))
							.password(rs.getString(6))
							.role(Role.valueOf(rs.getString(7)))
							.build());
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return Optional.empty();
	}
	/**
	 * Returns a {@code User}, if such exists in database. If not, returns {@code null}
	 * @param username - used to identify user in database
	 * @param password - used to identify user in database
	 * @return {@code User} or null, if not found
	 */
	public Optional<User> getUserByUsernameAndPassword(String username, String password) {
		String query = "SELECT `Users`.id, `Users`.username, `Users`.name, `Users`.surname, `Users`.rating, `Users`.password, user_roles.name FROM `Users` JOIN user_roles ON `Users`.role_id = user_roles.id WHERE `Users`.username=? AND `Users`.password=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, username);
			ps.setString(2, password);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Optional.of(User.builder()
							.id(UUID.fromString(rs.getString(1)))
							.username(rs.getString(2))
							.name(rs.getString(3))
							.surname(rs.getString(4))
							.rating(rs.getFloat(5))
							.password(rs.getString(6))
							.role(Role.valueOf(rs.getString(7)))
							.build());
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return Optional.empty();
	}
	
	
	
	public Optional<User> getUserByToken(String token) {
		String query = "SELECT `Users`.id, `Users`.username, `Users`.name, `Users`.surname, `Users`.rating, user_roles.name, Tokens.token, Tokens.refreshToken FROM `Users` JOIN Tokens ON Tokens.user_id = `Users`.id JOIN user_roles ON `Users`.role_id = user_roles.id WHERE Tokens.token=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setString(1, token);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Optional.of(User.builder()
							.id(UUID.fromString(rs.getString(1)))
							.username(rs.getString(2))
							.name(rs.getString(3))
							.surname(rs.getString(4))
							.rating(rs.getFloat(5))
							.role(Role.valueOf(rs.getString(6)))
							.token(rs.getString(7))
							.refreshToken(rs.getString(8))
							.build());
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return Optional.empty();
	}
	
	
	public List<User> getAllUsers(){
		List<User> result = new ArrayList<>();
		Connection connection = getNewConnection();
		String query = "SELECT `Users`.id, `Users`.username, `Users`.name, `Users`.surname, `Users`.rating, user_roles.name FROM `Users` JOIN Tokens ON Tokens.user_id = `Users`.id JOIN user_roles ON `Users`.role_id = user_roles.id";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					result.add(User.builder()
							.id(UUID.fromString(rs.getString(1)))
							.username(rs.getString(2))
							.name(rs.getString(3))
							.surname(rs.getString(4))
							.rating(rs.getFloat(5))
							.role(Role.valueOf(rs.getString(6)))
							.build());
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

}
