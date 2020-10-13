package test.java.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import main.java.entity.Car;
import main.java.entity.Status;
import main.java.repository.CarRepository;

public class CarRepositoryMock implements CarRepository{

	private Map<Integer, Car> cars = new HashMap<>();
	
	
	private CoordinateRepositoryMock coordRep = new CoordinateRepositoryMock();
	
	public CarRepositoryMock() {
		Car car1 = Car.builder()
				.id(1)
				.category("ISOLATED")
				.manufacturer("Honda")
				.model("Civic")
				.plate("AA0000AA")
				.passengerCount(4)
				.priceMultiplier(1)
				.status(Status.FREE)
				.coordinates(coordRep.getCoordinatesById(1).get())
				.build();
		cars.put(car1.getId(), car1);
	}
	
	@Override
	public List<Car> getAllActiveCars(String userLocale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Car> getAllCars(String userLocale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Car> getCarByOrderId(int orderId, String userLocale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCarStatus(int carid, int status) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Optional<String> getCategoryByLocale(String category, String locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Car> getAllCarsByPlacesCountAndCategory(int passengerCount, String carCategory, String userLocale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Car> getAllCarsByPlacesCount(int passengerCount, String userLocale) {
		// TODO Auto-generated method stub
		return null;
	}

}
