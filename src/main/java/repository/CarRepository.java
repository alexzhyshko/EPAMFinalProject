package main.java.repository;

import application.connection.DBConnectionManager;
import application.context.annotation.Component;
import application.context.annotation.Inject;

@Component
public class CarRepository {

	@Inject
	private DBConnectionManager connectionManager;
	
}
