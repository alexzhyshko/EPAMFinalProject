package main.java.service;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.repository.DriverRepository;

@Component
public class DriverService {

	@Inject
	private DriverRepository driverRepository;
	
}
