package main.java.service;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.Car;
import main.java.dto.Driver;
import main.java.exception.DriverNotFoundException;
import main.java.repository.DriverRepository;

@Component
public class DriverService {

	@Inject
	private DriverRepository driverRepository;
	
	public Driver getDriverByCar(Car car) {
		Driver driver = driverRepository.getDriverByCar(car);
		if(driver == null) {
			throw new DriverNotFoundException("Driver couldn't be found for the car with id="+car.getId());
		}
		return driver;
	}
	
}
