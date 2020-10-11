package main.java.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import application.connection.DBConnectionManager;
import application.context.annotation.component.Component;
import main.java.entity.Coordinates;

@Component
public class CoordinateRepository {

	private Connection getNewConnection() {
		return DBConnectionManager.getConnection();
	}

	public Optional<Integer> insertCoordinatesAndReturnId(Coordinates coordinates) {
		String insertCoordAndGetId = "INSERT INTO Coordinates(longitude, latitude) VALUES(?,?);";
		String selectLastInsertId = "SELECT LAST_INSERT_ID();";
		Connection connection = getNewConnection();
		try (PreparedStatement insertDeparture = connection.prepareStatement(insertCoordAndGetId);
				PreparedStatement selectDepartureId = connection.prepareStatement(selectLastInsertId)) {
			insertDeparture.setString(1, coordinates.getLongitude());
			insertDeparture.setString(2, coordinates.getLatitude());
			insertDeparture.executeUpdate();
			connection.commit();
			try (ResultSet result = selectDepartureId.executeQuery()) {
				while (result.next()) {
					return Optional.of(result.getInt(1));
				}
				connection.commit();
				return Optional.empty();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public Optional<Coordinates> getCoordinatesById(int coordinatesId) {
		String query = "SELECT longitude, latitude FROM Coordinates WHERE id=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, coordinatesId);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					return Optional.of(new Coordinates(rs.getString(1), rs.getString(2)));
				}
				connection.commit();
				return Optional.empty();
			} 
		}catch (SQLException e) {
			e.printStackTrace();
			return Optional.empty();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

}
