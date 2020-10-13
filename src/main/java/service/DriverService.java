package main.java.service;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import main.java.entity.Car;
import main.java.entity.Driver;
import main.java.repository.DriverRepository;

@Component
public class DriverService {

	@Inject("DriverRepositoryImpl")
	public DriverRepository driverRepository;
	
	public Driver getDriverByCar(Car car) {
		return driverRepository.getDriverByCar(car).orElseThrow(()-> new NullPointerException("No driver found for car"));
	}
	
	public Driver getDriverByOrderId(int id) {
		return driverRepository.getDriverByOrderId(id).orElseThrow(()-> new NullPointerException("No driver found for order"));
	}
	
}
