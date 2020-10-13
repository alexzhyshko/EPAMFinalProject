package test.java.mock;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import main.java.entity.Coordinates;
import main.java.repository.CoordinateRepository;

public class CoordinateRepositoryMock implements CoordinateRepository{

	
	private Map<Integer, Coordinates> coordinates = new HashMap<>();
	
	public CoordinateRepositoryMock() {
		Coordinates coord1 = new Coordinates("50.4939274", "30.4594769");
		Coordinates coord2 = new Coordinates("50.512468", "30.495047");
		coordinates.put(1, coord1);
		coordinates.put(2, coord2);
	}
	
	@Override
	public Optional<Integer> insertCoordinatesAndReturnId(Coordinates coordinates) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Coordinates> getCoordinatesById(int coordinatesId) {
		// TODO Auto-generated method stub
		return null;
	}

}
