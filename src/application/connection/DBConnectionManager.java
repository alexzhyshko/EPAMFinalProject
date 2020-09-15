package application.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import application.context.annotation.Component;
import application.context.reader.PropertyReader;

@Component
public class DBConnectionManager {

	private PropertyReader propertyReader = new PropertyReader();

	private String connectionURL;
	private String user;
	private String password;

	public DBConnectionManager() {
		this.connectionURL = propertyReader.getProperty("database.connection.url");
		this.user = propertyReader.getProperty("database.user");
		this.password = propertyReader.getProperty("database.password");
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public Connection getConnection() {
		try {
			Connection connection = DriverManager.getConnection(this.connectionURL, this.user, this.password);
			connection.setAutoCommit(false);
			return connection;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException("Connection couldn't be initialized");
		}
	}

}
