package main.java.repository;

import java.util.List;
import java.util.Optional;

import main.java.entity.Car;

public interface CarRepository {

	List<Car> getAllActiveCars(String userLocale);
	List<Car> getAllCars(String userLocale);
	Optional<Car> getCarByOrderId(int orderId, String userLocale);
	void setCarStatus(int carid, int status);
	Optional<String> getCategoryByLocale(String category, String locale);
	List<Car> getAllCarsByPlacesCountAndCategory(int passengerCount, String carCategory, String userLocale);
	List<Car> getAllCarsByPlacesCount(int passengerCount, String userLocale);
	
	
}
