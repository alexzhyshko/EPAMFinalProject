package main.java.service;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.repository.CarRepository;

@Component
public class CarService {

	@Inject
	private CarRepository carRepository;
	
}
