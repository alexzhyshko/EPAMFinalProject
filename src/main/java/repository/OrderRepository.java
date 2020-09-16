package main.java.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.connection.DBConnectionManager;
import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.Car;
import main.java.dto.Driver;
import main.java.dto.Order;
import main.java.dto.Route;
import main.java.dto.User;

@Component
public class OrderRepository {

	@Inject
	private DBConnectionManager connectionManager;

	private Connection getNewConnection() {
		return this.connectionManager.getConnection();
	}

	public Order tryCreateOrder(Route route, User customer, Driver driver, Car car, float price) {
		Connection connection = getNewConnection();
		String insertCoordAndGetId = "INSERT INTO Coordinates(longitude, latitude) VALUES(?,?);";
		String selectCoordId = "SELECT LAST_INSERT_ID();";
		int departureCoordId = -1;
		int destinationCoordId = -1;
		try (PreparedStatement insertDeparture = connection.prepareStatement(insertCoordAndGetId); PreparedStatement selectDepartureId = connection.prepareStatement(selectCoordId)) {
			insertDeparture.setString(1, route.departure.longitude);
			insertDeparture.setString(2, route.departure.latitude);
			insertDeparture.executeUpdate();
			connection.commit();
			try (ResultSet result = selectDepartureId.executeQuery()) {
				while (result.next()) {
					departureCoordId = result.getInt(1);
				}
				connection.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		try (PreparedStatement insertDeparture = connection.prepareStatement(insertCoordAndGetId); PreparedStatement selectDestinationId = connection.prepareStatement(selectCoordId)) {
			insertDeparture.setString(1, route.destination.longitude);
			insertDeparture.setString(2, route.destination.latitude);
			insertDeparture.executeUpdate();
			connection.commit();
			try (ResultSet result = selectDestinationId.executeQuery()) {
				while (result.next()) {
					destinationCoordId = result.getInt(1);
				}
				connection.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		
		String query = "INSERT INTO Orders(driving_id, user_id, departure_coordinate_id, destination_coordinate_id, price, distance, timeOccupancy, dateOfOrder, status_id) VALUES((SELECT id FROM Driving WHERE driver_id=? AND car_id=?), ?, ?, ?, ?, ?, ?, NOW(), 1)";

		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, driver.getId());
			ps.setInt(2, car.getId());
			ps.setString(3, customer.getId().toString());
			ps.setInt(4, departureCoordId);
			ps.setInt(5, destinationCoordId);
			ps.setFloat(6, price);
			ps.setFloat(7, route.distance);
			ps.setInt(8, route.time);
			ps.executeUpdate();
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return null;
			}
			e.printStackTrace();
			return null;
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		Order result = new Order();
		result.car = car;
		result.driver = driver;
		result.price = price;
		result.customer = customer;
		result.route = route;
		return result;
	}

}
