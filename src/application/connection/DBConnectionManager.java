package application.connection;

import java.sql.Connection;
import java.sql.SQLException;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import application.context.annotation.Component;

@Component
public class DBConnectionManager {
	private static final String DATASOURCE_NAME = "jdbc/conn";
	private static BasicDataSource ds = null;

	static {
		Context initContext;
		try {
			initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			ds = (BasicDataSource) envContext.lookup(DATASOURCE_NAME);
		} catch (NamingException e) {
			e.printStackTrace();
		}
	}

	public static Connection getConnection() {
		Connection conn;
		try {
			conn = ds.getConnection();
			return conn;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		throw new NullPointerException("Could not generate datasource");
	}
}
