package main.java.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import application.connection.DBConnectionManager;
import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.Car;
import main.java.dto.Coordinates;

@Component
public class CarRepository {

	@Inject
	private DBConnectionManager connectionManager;
	
	private Connection getNewConnection() {
		return this.connectionManager.getConnection();
	}
	
	public Car getCarByPlacesCountAndCategory(int passengerCount, String carCategory, String userLocale) {
		userLocale = userLocale.toUpperCase();
		String query = "SELECT Cars.id, Cars.plate, Manufacturers.name, Models.name, Cars.price_multiplier, Translations.text_"+userLocale+", Cars.passengerCount, Coordinates.longitude, Coordinates.latitude FROM Cars "
				+ " JOIN Manufacturers ON Cars.manufacturer_id = Manufacturers.id"
				+ " JOIN Models ON Cars.model_id = Models.id"
				+ " JOIN Translations ON Cars.category_translation_id = Translations.id"
				+ " JOIN Coordinates ON Cars.coordinates_id = Coordinates.id"
				+ " JOIN Driving ON Cars.id = Driving.car_id"
				+ " WHERE Cars.passengerCount=? AND Cars.status_id=1 AND UPPER(Translations.text_"+userLocale+")=? AND Driving.dayOfDriving=CURDATE()";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, passengerCount);
			ps.setString(2, carCategory);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Car.builder()
							.id(rs.getInt(1))
							.plate(rs.getString(2))
							.manufacturer(rs.getString(3))
							.model(rs.getString(4))
							.priceMultiplier(rs.getFloat(5))
							.category(rs.getString(6))
							.passengerCount(rs.getInt(7))
							.coordinates(new Coordinates(rs.getString(8), rs.getString(9)))
							.build();
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	
	public Car getCarByPlacesCount(int passengerCount, String userLocale) {
		userLocale = userLocale.toUpperCase();
		String query = "SELECT Cars.id, Cars.plate, Manufacturers.name, Models.name, Cars.price_multiplier, Translations.text_"+userLocale+", Cars.passengerCount, Coordinates.longitude, Coordinates.latitude FROM Cars "
				+ " JOIN Manufacturers ON Cars.manufacturer_id = Manufacturers.id"
				+ " JOIN Models ON Cars.model_id = Models.id"
				+ " JOIN Translations ON Cars.category_translation_id = Translations.id"
				+ " JOIN Coordinates ON Cars.coordinates_id = Coordinates.id"
				+ " JOIN Driving ON Cars.id = Driving.car_id"
				+ " WHERE Cars.passengerCount>=? AND Cars.status_id=1 AND Driving.dayOfDriving=CURDATE()";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, passengerCount);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Car.builder()
							.id(rs.getInt(1))
							.plate(rs.getString(2))
							.manufacturer(rs.getString(3))
							.model(rs.getString(4))
							.priceMultiplier(rs.getFloat(5))
							.category(rs.getString(6))
							.passengerCount(rs.getInt(7))
							.coordinates(new Coordinates(rs.getString(8), rs.getString(9)))
							.build();
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public Car getCarByOrderId(int orderId, String userLocale) {
		String query = "SELECT Cars.id, Cars.plate, Manufacturers.name, Models.name, Cars.price_multiplier, Translations.text_"+userLocale+", Cars.passengerCount, Coordinates.longitude, Coordinates.latitude FROM Cars "
				+ " JOIN Manufacturers ON Cars.manufacturer_id = Manufacturers.id"
				+ " JOIN Models ON Cars.model_id = Models.id"
				+ " JOIN Translations ON Cars.category_translation_id = Translations.id"
				+ " JOIN Coordinates ON Cars.coordinates_id = Coordinates.id"
				+ " JOIN Driving ON Cars.id = Driving.car_id"
				+ " JOIN Orders ON Driving.id = Orders.driving_id"
				+ " WHERE Orders.id=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, orderId);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Car.builder()
							.id(rs.getInt(1))
							.plate(rs.getString(2))
							.manufacturer(rs.getString(3))
							.model(rs.getString(4))
							.priceMultiplier(rs.getFloat(5))
							.category(rs.getString(6))
							.passengerCount(rs.getInt(7))
							.coordinates(new Coordinates(rs.getString(8), rs.getString(9)))
							.build();
				}
			}
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
		}finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return null;
		
	}
	
	public void setCarStatus(int carid, int status) {
		String updateCarStatus = "UPDATE Cars SET status_id=? WHERE id=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(updateCarStatus)) {
			ps.setInt(1, status);
			ps.setInt(2, carid);
			ps.executeUpdate();
			connection.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
