package main.java.exception;

public class DriverNotFoundException extends RuntimeException{

	public DriverNotFoundException(String message) {
		super(message);
	}
	
}
