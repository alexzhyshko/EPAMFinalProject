package main.java;

import application.context.annotation.Component;
import application.context.annotation.Inject;

@Component
public class Service {

	@Inject
	private Repository repository;
	
	public String getUsername() {
		return repository.getUsername();
	}
	
}
