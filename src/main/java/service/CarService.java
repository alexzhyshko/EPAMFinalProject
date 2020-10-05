package main.java.service;

import java.util.List;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.entity.Car;
import main.java.entity.CarCategory;
import main.java.entity.Coordinates;
import main.java.exception.NoSuitableCarFound;
import main.java.repository.CarRepository;

@Component
public class CarService {

	@Inject
	private CarRepository carRepository;

	public List<Car> getAllAvailableCars(String userLocale){
		return carRepository.getAllActiveCars(userLocale);
	}
	
	public Car getNearestCarByPlacesCountAndCategory(int placesCount, String carCategory, String userLocale,
			Coordinates clientCoord) {
		List<Car> allSuitableCars = carRepository.getAllCarsByPlacesCountAndCategory(placesCount, carCategory,
				userLocale);
		Car result = null;
		double minDistance = Double.MAX_VALUE;
		for (Car car : allSuitableCars) {
			// calculate distance between client and car
			double distance = distanceInKmBetweenEarthCoordinates(Double.parseDouble(car.getCoordinates().getLatitude()),
					Double.parseDouble(car.getCoordinates().getLongitude()), Double.parseDouble(clientCoord.getLatitude()),
					Double.parseDouble(clientCoord.getLongitude()));
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
			double distance = distanceInKmBetweenEarthCoordinates(Double.parseDouble(car.getCoordinates().getLatitude()),
					Double.parseDouble(car.getCoordinates().getLongitude()), Double.parseDouble(clientCoord.getLatitude()),
					Double.parseDouble(clientCoord.getLongitude()));
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
		return carRepository.getCarByOrderId(orderId, userLocale).orElseThrow(()-> new NullPointerException("No car found for id"));
	}

	public void setCarStatus(int carid, int status) {
		carRepository.setCarStatus(carid, status);
	}

	public String getCategoryByLocale(CarCategory category, String locale) {
		return carRepository.getCategoryByLocale(category.toString(), locale).orElseThrow(()->new NullPointerException("No category found for car and locale"));
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
