package main.java.service;

import java.util.List;
import java.util.Optional;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import main.java.entity.Car;
import main.java.entity.CarCategory;
import main.java.entity.Coordinates;
import main.java.exception.NoSuitableCarFound;
import main.java.repository.CarRepository;

@Component
public class CarService {

	@Inject("CarRepositoryImpl")
	public CarRepository carRepository;

	private static final int MAX_DISTANCE_CONSTRAINT_KM = 100;
	
	public List<Car> getAllAvailableCars(String userLocale){
		return carRepository.getAllActiveCars(userLocale);
	}
	
	public Car getNearestCarByPlacesCountAndCategory(int placesCount, String carCategory, String userLocale,
			Coordinates clientCoord) {
		List<Car> allSuitableCars = carRepository.getAllCarsByPlacesCountAndCategory(placesCount, carCategory,
				userLocale);
		return getNearestCarToClientAccountingDistanceConstraint(placesCount, allSuitableCars, clientCoord);
	}

	public Car getNearestCarByPlacesCount(int placesCount, String userLocale, Coordinates clientCoord) {
		List<Car> allSuitableCars = carRepository.getAllCarsByPlacesCount(placesCount,
				userLocale);
		return getNearestCarToClientAccountingDistanceConstraint(placesCount, allSuitableCars, clientCoord);
	}


	private Car getNearestCarToClientAccountingDistanceConstraint(int placesCount, List<Car> allSuitableCars, Coordinates clientCoord) {
		Car result = getNearestCarToClient(allSuitableCars, clientCoord).orElseThrow(()->new NoSuitableCarFound("No cars found for " + placesCount + " places"));
		if (calculateDistanceBetweenClientAndCar(clientCoord, result) > MAX_DISTANCE_CONSTRAINT_KM) {
			throw new NoSuitableCarFound(
					"No cars found for " + placesCount + " places within "+MAX_DISTANCE_CONSTRAINT_KM+" km");
		}
		return result;
	}
	private Optional<Car> getNearestCarToClient(List<Car> allSuitableCars, Coordinates clientCoord) {
		Optional<Car> result = Optional.empty();
		double minDistance = Double.MAX_VALUE;
		for (Car car : allSuitableCars) {
			double distance = calculateDistanceBetweenClientAndCar(clientCoord, car);
			if (distance < minDistance) {
				minDistance = distance;
				result = Optional.of(car);
			}
		}
		return result;
	}

	private double calculateDistanceBetweenClientAndCar(Coordinates clientCoord, Car car) {
		return distanceInKmBetweenEarthCoordinates(Double.parseDouble(car.getCoordinates().getLatitude()),
				Double.parseDouble(car.getCoordinates().getLongitude()), Double.parseDouble(clientCoord.getLatitude()),
				Double.parseDouble(clientCoord.getLongitude()));
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
