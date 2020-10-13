package application.connection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.tomcat.dbcp.dbcp2.BasicDataSource;

import application.context.annotation.component.Component;

@Component
public class DBConnectionManager {
	private static final String DATASOURCE_NAME = "jdbc/conn";
	private static BasicDataSource ds = null;

	static Logger logger = Logger.getLogger("application");
	
	static {
		Context initContext;
		try {
			initContext = new InitialContext();
			Context envContext = (Context) initContext.lookup("java:/comp/env");
			ds = (BasicDataSource) envContext.lookup(DATASOURCE_NAME);
		} catch (NamingException e) {
			logger.severe(e.getMessage()+"\n"+e.getExplanation());
		}
	}

	public static Connection getConnection() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			logger.severe(e.getMessage());
		}
		throw new NullPointerException("Could not generate datasource");
	}
}
