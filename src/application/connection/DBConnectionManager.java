package application.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.reader.PropertyReader;

@Component
public class DBConnectionManager {

	@Inject
	private PropertyReader propertyReader;

	private String connectionURL;

	public DBConnectionManager() {
		this.connectionURL = propertyReader.getProperty("database.connection.url");
	}

	public Connection getConnection() {
		try (Connection connection = DriverManager.getConnection(this.connectionURL)) {
			return connection;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new NullPointerException("A new connection couldn't be initialized");
		}
	}

}
