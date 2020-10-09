package main.java.exception;

public class CouldNotCreateOrderException extends RuntimeException{

	public CouldNotCreateOrderException(String message) {
		super(message);
	}
	
}
