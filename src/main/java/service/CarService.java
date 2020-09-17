package main.java.service;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.Car;
import main.java.exception.NoSuitableCarFound;
import main.java.repository.CarRepository;

@Component
public class CarService {

	@Inject
	private CarRepository carRepository;
	
	public Car getCarByPlacesCountAndCategory(int placesCount, String carCategory, String userLocale) {
		Car car = carRepository.getCarByPlacesCountAndCategory(placesCount, carCategory, userLocale);
		if(car == null) {
			throw new NoSuitableCarFound("No car found for category "+carCategory+" and "+placesCount+" places");
		}
		return car;
	}
	
	public Car getCarByPlacesCount(int placesCount, String userLocale) {
		Car car = carRepository.getCarByPlacesCount(placesCount, userLocale);
		if(car == null) {
			throw new NoSuitableCarFound("No car found for "+placesCount+" places");
		}
		return car;
	}
	
	
	public Car getCarByOrderId(int orderId, String userLocale) {
		return carRepository.getCarByOrderId(orderId, userLocale);
	}
	
	public void setCarStatus(int carid, int status) {
		carRepository.setCarStatus(carid, status);
	}
	
}
