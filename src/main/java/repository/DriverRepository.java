package main.java.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import application.connection.DBConnectionManager;
import application.context.annotation.component.Component;
import main.java.entity.Car;
import main.java.entity.Driver;

@Component
public class DriverRepository {

	private Connection getNewConnection() {
		return DBConnectionManager.getConnection();
	}
	
	public Optional<Driver> getDriverByCar(Car car) {
		String query = "SELECT Drivers.id, Drivers.name, Drivers.surname, Drivers.rating FROM Drivers JOIN Driving ON Driving.driver_id = Drivers.id WHERE Driving.car_id=? AND Driving.dayOfDriving=CURDATE()";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, car.getId());
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Optional.of(Driver.builder()
							.id(rs.getInt(1))
							.name(rs.getString(2))
							.surname(rs.getString(3))
							.rating(rs.getFloat(4))
							.build());
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
		return Optional.empty();
	}
	
	
	public Optional<Driver> getDriverByOrderId(int id) {
		String query = "SELECT Drivers.id, Drivers.name, Drivers.surname, Drivers.rating FROM Drivers JOIN Driving ON Driving.driver_id=Drivers.id JOIN Orders ON Orders.driving_id = Driving.id WHERE Orders.id=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, id);
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					return Optional.of(Driver.builder()
							.id(rs.getInt(1))
							.name(rs.getString(2))
							.surname(rs.getString(3))
							.rating(rs.getFloat(4))
							.build());
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
		return Optional.empty();
	}
	
}
