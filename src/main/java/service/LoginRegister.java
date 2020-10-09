package main.java.service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class LoginRegister {

	private static Set<String> loggedInUsers = new HashSet<>();

	private LoginRegister() {}
	
	public static Optional<String> addToRegisterIfNotLoggedIn(String username){
		if(loggedInUsers.add(username))
			return Optional.of(username);
		return Optional.empty();
	}
	
	
	public static Optional<String> removeFromRegisterIfLoggedIn(String username){
		if(loggedInUsers.remove(username))
			return Optional.of(username);
		return Optional.empty();
	}
}
