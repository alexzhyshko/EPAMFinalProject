package main.java.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import application.connection.DBConnectionManager;
import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.entity.Car;
import main.java.entity.Coordinates;
import main.java.entity.Driver;
import main.java.entity.Order;
import main.java.entity.Route;
import main.java.entity.User;
import main.java.service.CarService;
import main.java.service.DriverService;
import main.java.service.UserService;

@Component
public class OrderRepository {

	@Inject
	private DBConnectionManager connectionManager;

	@Inject
	private CarService carService;

	@Inject
	private UserService userService;

	@Inject
	private DriverService driverService;

	@Inject
	private CoordinateRepository coordinateRepository;

	private Connection getNewConnection() {
		return this.connectionManager.getConnection();
	}

	public Order tryCreateOrder(Route route, User customer, Driver driver, Car car, float price) {
		Connection connection = getNewConnection();
		int departureCoordId = coordinateRepository.insertCoordinatesAndReturnId(route.departure);
		int destinationCoordId = coordinateRepository.insertCoordinatesAndReturnId(route.destination);
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
		}

		int orderId = -1;
		String selectLastInsertId = "SELECT LAST_INSERT_ID();";
		try (PreparedStatement selectOrderId = connection.prepareStatement(selectLastInsertId);
				ResultSet result = selectOrderId.executeQuery()) {
			while (result.next()) {
				orderId = result.getInt(1);
			}
			connection.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return null;
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Order result = new Order();
		result.id = orderId;
		result.car = car;
		result.driver = driver;
		result.price = price;
		result.customer = customer;
		result.route = route;
		return result;
	}

	public boolean finishOrder(int orderId) {
		String query = "UPDATE Orders SET status_id=2 WHERE id=?";
		Connection connection = getNewConnection();
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, orderId);
			ps.executeUpdate();
			connection.commit();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return false;
			}
			return false;
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	public List<Order> getAllOrdersByStatusAndUser(UUID userid, int status, String userLocale) {
		List<Order> result = new ArrayList<>();

		String query = "SELECT Orders.id, price, distance, timeOccupancy, dateOfOrder, departure_coordinate_id, destination_coordinate_id, Translations.text_"+userLocale+" FROM Orders JOIN order_status ON order_status.id = orders.status_id JOIN Translations ON Translations.id = order_status.name_translations_id WHERE user_id=? AND status_id=?";
		Connection connection = getNewConnection();

		int departureCoordId = -1;
		int destinationCoordId = -1;

		try (PreparedStatement getOrderIds = connection.prepareStatement(query)) {
			getOrderIds.setString(1, userid.toString());
			getOrderIds.setInt(2, status);
			try (ResultSet rs = getOrderIds.executeQuery()) {
				while (rs.next()) {
					Order order = new Order();
					order.id = rs.getInt(1);
					order.price = rs.getFloat(2);
					order.statusid = status;
					order.status = rs.getString(8);
					Route route = new Route();
					route.distance = rs.getFloat(3);
					route.time = rs.getInt(4);
					order.route = route;
					order.dateOfOrder = rs.getTimestamp(5).toLocalDateTime();
					departureCoordId = rs.getInt(6);
					destinationCoordId = rs.getInt(7);
					result.add(order);
				}
				connection.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		}
		User user = userService.getUserById(userid);
		for (Order order : result) {
			order.customer = user;
			Car car = carService.getCarByOrderId(order.id, userLocale);
			order.car = car;
			Driver driver = driverService.getDriverByOrderId(order.id);
			order.driver = driver;
			Coordinates departure = coordinateRepository.getCoordinatesById(departureCoordId);
			Coordinates destination = coordinateRepository.getCoordinatesById(destinationCoordId);
			order.route.departure = departure;
			order.route.destination = destination;
		}
		return result;
	}

	public Order getOrderById(int orderId, String userLocale) {
		String query = "SELECT id, price, distance, timeOccupancy, dateOfOrder, departure_coordinate_id, destination_coordinate_id, user_id, status_id FROM Orders WHERE id=?";
		Connection connection = getNewConnection();
		Order order = new Order();
		int departureCoordId = -1;
		int destinationCoordId = -1;
		UUID userid = null;
		try (PreparedStatement getOrderIds = connection.prepareStatement(query)) {
			getOrderIds.setInt(1, orderId);
			try (ResultSet rs = getOrderIds.executeQuery()) {
				while (rs.next()) {
					order.id = rs.getInt(1);
					order.price = rs.getFloat(2);
					order.statusid = rs.getInt(9);
					Route route = new Route();
					route.distance = rs.getFloat(3);
					route.time = rs.getInt(4);
					order.route = route;
					order.dateOfOrder = rs.getTimestamp(5).toLocalDateTime();
					departureCoordId = rs.getInt(6);
					destinationCoordId = rs.getInt(7);
					userid = UUID.fromString(rs.getString(8));
				}
				connection.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
		User user = userService.getUserById(userid);
		order.customer = user;
		Car car = carService.getCarByOrderId(order.id, userLocale);
		order.car = car;
		Driver driver = driverService.getDriverByCar(car);
		order.driver = driver;
		Coordinates departure = coordinateRepository.getCoordinatesById(departureCoordId);
		Coordinates destination = coordinateRepository.getCoordinatesById(destinationCoordId);
		order.route.departure = departure;
		order.route.destination = destination;
		return order;
	}

}
