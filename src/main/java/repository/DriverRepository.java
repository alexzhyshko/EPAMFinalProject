package main.java.repository;

import java.util.Optional;

import main.java.entity.Car;
import main.java.entity.Driver;

public interface DriverRepository {

	Optional<Driver> getDriverByCar(Car car);
	Optional<Driver> getDriverByOrderId(int id);
	
	
}
