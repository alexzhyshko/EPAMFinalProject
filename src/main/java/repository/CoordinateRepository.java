package main.java.repository;

import java.util.Optional;

import main.java.entity.Coordinates;

public interface CoordinateRepository {

	Optional<Integer> insertCoordinatesAndReturnId(Coordinates coordinates);
	Optional<Coordinates> getCoordinatesById(int coordinatesId);
	
	
}
