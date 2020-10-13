package test.java.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import main.java.entity.Car;
import main.java.entity.Driver;
import main.java.repository.DriverRepository;

public class DriverRepositoryMock implements DriverRepository{

	
	private Map<Integer, Driver> drivers = new HashMap<>();
	
	public DriverRepositoryMock() {
		Driver driver = Driver.builder()
				.id(1)
				.name("Vasia")
				.surname("Pupkin")
				.rating(5)
				.build();
		drivers.put(driver.getId(), driver);
	}
	
	@Override
	public Optional<Driver> getDriverByCar(Car car) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Driver> getDriverByOrderId(int id) {
		// TODO Auto-generated method stub
		return null;
	}

}
