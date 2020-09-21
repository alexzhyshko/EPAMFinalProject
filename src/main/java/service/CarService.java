package main.java.service;

import java.util.List;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.Car;
import main.java.dto.CarCategory;
import main.java.dto.Coordinates;
import main.java.exception.NoSuitableCarFound;
import main.java.repository.CarRepository;

@Component
public class CarService {

	@Inject
	private CarRepository carRepository;

	public List<Car> getAllCars(String userLocale){
		return carRepository.getAllCars(userLocale);
	}
	
	public Car getNearestCarByPlacesCountAndCategory(int placesCount, String carCategory, String userLocale,
			Coordinates clientCoord) {
		List<Car> allSuitableCars = carRepository.getAllCarsByPlacesCountAndCategory(placesCount, carCategory,
				userLocale);
		Car result = null;
		double minDistance = Double.MAX_VALUE;
		for (Car car : allSuitableCars) {
			// calculate distance between client and car
			double distance = distanceInKmBetweenEarthCoordinates(Double.parseDouble(car.getCoordinates().latitude),
					Double.parseDouble(car.getCoordinates().longitude), Double.parseDouble(clientCoord.latitude),
					Double.parseDouble(clientCoord.longitude));
			if (distance < minDistance) {
				minDistance = distance;
				result = car;
			}
		}
		if(result == null) {
			throw new NoSuitableCarFound(
					"No cars found for " + placesCount + " places and category " + carCategory);
		}
		if (minDistance > 100) {
			throw new NoSuitableCarFound(
					"No cars found for " + placesCount + " places and category " + carCategory + " within 100 km");
		}
		return result;
	}


	public Car getNearestCarByPlacesCount(int placesCount, String userLocale, Coordinates clientCoord) {
		List<Car> allSuitableCars = carRepository.getAllCarsByPlacesCount(placesCount,
				userLocale);
		Car result = null;
		double minDistance = Double.MAX_VALUE;
		for (Car car : allSuitableCars) {
			// calculate distance between client and car
			double distance = distanceInKmBetweenEarthCoordinates(Double.parseDouble(car.getCoordinates().latitude),
					Double.parseDouble(car.getCoordinates().longitude), Double.parseDouble(clientCoord.latitude),
					Double.parseDouble(clientCoord.longitude));
			if (distance < minDistance) {
				minDistance = distance;
				result = car;
			}
		}
		if(result == null) {
			throw new NoSuitableCarFound(
					"No cars found for " + placesCount + " places");
		}
		if (minDistance > 100) {
			throw new NoSuitableCarFound(
					"No cars found for " + placesCount + " places within 100 km");
		}
		return result;
	}

	public Car getCarByOrderId(int orderId, String userLocale) {
		return carRepository.getCarByOrderId(orderId, userLocale);
	}

	public void setCarStatus(int carid, int status) {
		carRepository.setCarStatus(carid, status);
	}

	public String getCategoryByLocale(CarCategory category, String locale) {
		return carRepository.getCategoryByLocale(category.toString(), locale);
	}
	
	
	
	private double degreesToRadians(double degrees) {
		return degrees * Math.PI / 180;
	}

	private double distanceInKmBetweenEarthCoordinates(double lat1, double lon1, double lat2, double lon2) {
		int earthRadiusKm = 6371;

		double dLat = degreesToRadians(lat2 - lat1);
		double dLon = degreesToRadians(lon2 - lon1);

		lat1 = degreesToRadians(lat1);
		lat2 = degreesToRadians(lat2);

		double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
				+ Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(lat1) * Math.cos(lat2);
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
		return earthRadiusKm * c;
	}

}
