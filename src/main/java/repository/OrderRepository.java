package main.java.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import application.connection.DBConnectionManager;
import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.entity.Car;
import main.java.entity.Coordinates;
import main.java.entity.Driver;
import main.java.entity.Order;
import main.java.entity.Role;
import main.java.entity.Route;
import main.java.entity.User;
import main.java.exception.IncorrectDateFormatException;
import main.java.service.CarService;
import main.java.service.DriverService;
import main.java.service.UserService;

@Component
public class OrderRepository {


	@Inject
	private CarService carService;

	@Inject
	private UserService userService;

	@Inject
	private DriverService driverService;

	@Inject
	private CoordinateRepository coordinateRepository;

	private Connection getNewConnection() {
		return DBConnectionManager.getConnection();
	}

	public Optional<Order> tryCreateOrder(Route route, User customer, Driver driver, Car car, float price) {
		Connection connection = getNewConnection();
		int departureCoordId = coordinateRepository.insertCoordinatesAndReturnId(route.getDeparture())
				.orElseThrow(NullPointerException::new);
		int destinationCoordId = coordinateRepository.insertCoordinatesAndReturnId(route.getDestination())
				.orElseThrow(NullPointerException::new);
		String query = "INSERT INTO Orders(driving_id, user_id, departure_coordinate_id, destination_coordinate_id, price, distance, timeOccupancy, dateOfOrder, status_id) VALUES((SELECT id FROM Driving WHERE driver_id=? AND car_id=?), ?, ?, ?, ?, ?, ?, NOW(), 1)";
		try (PreparedStatement ps = connection.prepareStatement(query)) {
			ps.setInt(1, driver.getId());
			ps.setInt(2, car.getId());
			ps.setString(3, customer.getId().toString());
			ps.setInt(4, departureCoordId);
			ps.setInt(5, destinationCoordId);
			ps.setFloat(6, price);
			ps.setFloat(7, route.getDistance());
			ps.setInt(8, route.getTime());
			ps.executeUpdate();
			connection.commit();
		} catch (Exception e) {
			try {
				connection.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
				return Optional.empty();
			}
			e.printStackTrace();
			return Optional.empty();
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
				return Optional.empty();
			}
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

		Order result = Order.builder().id(orderId).car(car).driver(driver).price(price).customer(customer).route(route)
				.build();
		return Optional.of(result);
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

	public List<Order> getAllOrdersByStatusAndUser(UUID userid, int status, int skip, int limit, String userLocale) {
		List<Order> result = new ArrayList<>();
		String query = "select orders.id as 'orderId',\r\n" + "orders.price as 'orderPrice',\r\n"
				+ "orders.distance as 'orderDistance',\r\n" + "orders.timeOccupancy as 'orderTime',\r\n"
				+ "orders.dateOfOrder as 'orderDate',\r\n" + "order_depart_coord.longitude as 'orderDepartLng',\r\n"
				+ "order_depart_coord.latitude as 'orderDepartLat',\r\n"
				+ "order_dest_coord.longitude as 'orderDestLng',\r\n"
				+ "order_dest_coord.latitude as 'orderDestLat',\r\n" + "order_translation.text_" + userLocale
				+ " as 'orderStatus',\r\n" + "`Users`.id as 'userId',\r\n" + "`Users`.username as 'username',\r\n"
				+ "`Users`.name as 'userName',\r\n" + "`Users`.surname as 'userSurname',\r\n"
				+ "`Users`.rating as 'userRating',\r\n" + "user_roles.name as 'userRole',\r\n"
				+ "Cars.id as 'carId',\r\n" + "Cars.plate as 'carPlate',\r\n"
				+ "Manufacturers.name as 'carManufacturer',\r\n" + "Models.name as 'carModel',\r\n"
				+ "Cars.price_multiplier as 'carPriceMult',\r\n" + "car_translation.text_" + userLocale
				+ " as 'carCategory',\r\n" + "Cars.passengerCount as 'carPassengerCount',\r\n"
				+ "car_coord.longitude as 'carLng',\r\n" + "car_coord.latitude as 'carLat',\r\n"
				+ "Drivers.id as 'driverId',\r\n" + "Drivers.name as 'driverName',\r\n"
				+ "Drivers.surname as 'driverSurname',\r\n" + "Drivers.rating as 'driverRating'\r\n " + "\r\n"
				+ "FROM orders\r\n "
				+ "JOIN Coordinates order_depart_coord ON order_depart_coord.id = orders.departure_coordinate_id \r\n"
				+ "JOIN Coordinates order_dest_coord ON order_dest_coord.id = orders.destination_coordinate_id \r\n"
				+ "JOIN order_status ON order_status.id = orders.status_id \r\n"
				+ "JOIN Translations order_translation ON order_translation.id = order_status.name_translations_id \r\n"
				+ "JOIN `Users` ON orders.user_id = `Users`.id \r\n"
				+ "JOIN user_roles ON user_roles.id = `Users`.role_id \r\n"
				+ "JOIN Driving ON Orders.driving_id = Driving.id \r\n" + "JOIN Cars ON Cars.id = Driving.car_id \r\n"
				+ "JOIN Manufacturers ON Cars.manufacturer_id = Manufacturers.id \r\n"
				+ "JOIN Models ON Cars.model_id = Models.id \r\n"
				+ "JOIN Translations car_translation ON Cars.category_translation_id = car_translation.id \r\n"
				+ "JOIN Coordinates car_coord ON Cars.coordinates_id = car_coord.id \r\n"
				+ "JOIN Drivers ON Driving.driver_id = Drivers.id \r\n" + "\r\n"
				+ "WHERE orders.user_id=? AND orders.status_id=? \r\n" + "LIMIT ?,? \r\n";
		Connection connection = getNewConnection();
		try (PreparedStatement getOrderIds = connection.prepareStatement(query)) {
			getOrderIds.setString(1, userid.toString());
			getOrderIds.setInt(2, status);
			getOrderIds.setInt(3, skip);
			getOrderIds.setInt(4, limit - skip);
			try (ResultSet rs = getOrderIds.executeQuery()) {
				while (rs.next()) {
					
					Coordinates routeDeparture = new Coordinates(rs.getString("orderDepartLng"),
							rs.getString("orderDepartLat"));
					Coordinates routeDestination = new Coordinates(rs.getString("orderDestLng"),
							rs.getString("orderDestLat"));
					Route route = Route.builder()
							.distance(rs.getFloat("orderDistance"))
							.time(rs.getInt("orderTime"))
							.departure(routeDeparture)
							.destination(routeDestination)
							.build();
					User user = User.builder().id(UUID.fromString(rs.getString("userId")))
							.username(rs.getString("username")).name(rs.getString("userName"))
							.surname(rs.getString("userSurname")).rating(rs.getFloat("userRating"))
							.role(Role.valueOf(rs.getString("userRole"))).build();
					Car car = Car.builder().id(rs.getInt("carId")).plate(rs.getString("carPlate"))
							.manufacturer(rs.getString("carManufacturer")).model(rs.getString("carModel"))
							.category(rs.getString("carCategory")).passengerCount(rs.getInt("carPassengerCount"))
							.priceMultiplier(rs.getFloat("carPriceMult"))
							.coordinates(new Coordinates(rs.getString("carLng"), rs.getString("carLat"))).build();
					Driver driver = Driver.builder().id(rs.getInt("driverId")).name(rs.getString("driverName"))
							.surname(rs.getString("driverSurname")).rating(rs.getFloat("driverRating")).build();
					Order order = Order.builder().id(rs.getInt("orderId")).price(rs.getFloat("orderPrice"))
							.timeToArrival(rs.getInt("orderTime"))
							.dateOfOrder(rs.getTimestamp("orderDate").toLocalDateTime()).statusid(status)
							.status(rs.getString("orderStatus")).route(route).customer(user).car(car).driver(driver)
							.build();
					result.add(order);
				}
				connection.commit();
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return new ArrayList<>();
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public Optional<Order> getOrderById(int orderId, String userLocale) {
		String query = "select orders.id as 'orderId',\r\n" + "orders.price as 'orderPrice',\r\n"
				+ "orders.distance as 'orderDistance',\r\n" + "orders.timeOccupancy as 'orderTime',\r\n"
				+ "orders.dateOfOrder as 'orderDate',\r\n" + "order_depart_coord.longitude as 'orderDepartLng',\r\n"
				+ "order_depart_coord.latitude as 'orderDepartLat',\r\n"
				+ "order_dest_coord.longitude as 'orderDestLng',\r\n"
				+ "order_dest_coord.latitude as 'orderDestLat',\r\n" + "order_translation.text_" + userLocale
				+ " as 'orderStatus',\r\n" + "orders.status_id as 'orderStatusId',\r\n" + "`Users`.id as 'userId',\r\n"
				+ "`Users`.username as 'username',\r\n" + "`Users`.name as 'userName',\r\n"
				+ "`Users`.surname as 'userSurname',\r\n" + "`Users`.rating as 'userRating',\r\n"
				+ "user_roles.name as 'userRole',\r\n" + "Cars.id as 'carId',\r\n" + "Cars.plate as 'carPlate',\r\n"
				+ "Manufacturers.name as 'carManufacturer',\r\n" + "Models.name as 'carModel',\r\n"
				+ "Cars.price_multiplier as 'carPriceMult',\r\n" + "car_translation.text_" + userLocale
				+ " as 'carCategory',\r\n" + "Cars.passengerCount as 'carPassengerCount',\r\n"
				+ "car_coord.longitude as 'carLng',\r\n" + "car_coord.latitude as 'carLat',\r\n"
				+ "Drivers.id as 'driverId',\r\n" + "Drivers.name as 'driverName',\r\n"
				+ "Drivers.surname as 'driverSurname',\r\n" + "Drivers.rating as 'driverRating'\r\n " + "\r\n"
				+ "FROM orders\r\n "
				+ "JOIN Coordinates order_depart_coord ON order_depart_coord.id = orders.departure_coordinate_id \r\n"
				+ "JOIN Coordinates order_dest_coord ON order_dest_coord.id = orders.destination_coordinate_id \r\n"
				+ "JOIN order_status ON order_status.id = orders.status_id \r\n"
				+ "JOIN Translations order_translation ON order_translation.id = order_status.name_translations_id \r\n"
				+ "JOIN `Users` ON orders.user_id = `Users`.id \r\n"
				+ "JOIN user_roles ON user_roles.id = `Users`.role_id \r\n"
				+ "JOIN Driving ON Orders.driving_id = Driving.id \r\n" + "JOIN Cars ON Cars.id = Driving.car_id \r\n"
				+ "JOIN Manufacturers ON Cars.manufacturer_id = Manufacturers.id \r\n"
				+ "JOIN Models ON Cars.model_id = Models.id \r\n"
				+ "JOIN Translations car_translation ON Cars.category_translation_id = car_translation.id \r\n"
				+ "JOIN Coordinates car_coord ON Cars.coordinates_id = car_coord.id \r\n"
				+ "JOIN Drivers ON Driving.driver_id = Drivers.id \r\n" + "WHERE Orders.id=?";
		Connection connection = getNewConnection();

		try (PreparedStatement getOrderIds = connection.prepareStatement(query)) {
			getOrderIds.setInt(1, orderId);
			try (ResultSet rs = getOrderIds.executeQuery()) {
				while (rs.next()) {
					Coordinates routeDeparture = new Coordinates(rs.getString("orderDepartLng"),
							rs.getString("orderDepartLat"));
					Coordinates routeDestination = new Coordinates(rs.getString("orderDestLng"),
							rs.getString("orderDestLat"));
					Route route = Route.builder()
							.distance(rs.getFloat("orderDistance"))
							.time(rs.getInt("orderTime"))
							.departure(routeDeparture)
							.destination(routeDestination)
							.build();
					User user = User.builder().id(UUID.fromString(rs.getString("userId")))
							.username(rs.getString("username")).name(rs.getString("userName"))
							.surname(rs.getString("userSurname")).rating(rs.getFloat("userRating"))
							.role(Role.valueOf(rs.getString("userRole"))).build();
					Car car = Car.builder().id(rs.getInt("carId")).plate(rs.getString("carPlate"))
							.manufacturer(rs.getString("carManufacturer")).model(rs.getString("carModel"))
							.category(rs.getString("carCategory")).passengerCount(rs.getInt("carPassengerCount"))
							.priceMultiplier(rs.getFloat("carPriceMult"))
							.coordinates(new Coordinates(rs.getString("carLng"), rs.getString("carLat"))).build();
					Driver driver = Driver.builder().id(rs.getInt("driverId")).name(rs.getString("driverName"))
							.surname(rs.getString("driverSurname")).rating(rs.getFloat("driverRating")).build();
					Order order = Order.builder().id(rs.getInt("orderId")).price(rs.getFloat("orderPrice"))
							.timeToArrival(rs.getInt("orderTime"))
							.dateOfOrder(rs.getTimestamp("orderDate").toLocalDateTime())
							.statusid(rs.getInt("orderStatusId")).status(rs.getString("orderStatus")).route(route)
							.customer(user).car(car).driver(driver).build();
					return Optional.of(order);
				}
				connection.commit();
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
		return Optional.empty();

	}

	public List<Order> getAllOrders(String userLocale, String filterBy, String value, int skip, int limit,
			boolean filter) {
		List<Order> result = new ArrayList<>();
		String query = "select orders.id as 'orderId',\r\n" + "orders.price as 'orderPrice',\r\n"
				+ "orders.distance as 'orderDistance',\r\n" + "orders.timeOccupancy as 'orderTime',\r\n"
				+ "orders.dateOfOrder as 'orderDate',\r\n" + "order_depart_coord.longitude as 'orderDepartLng',\r\n"
				+ "order_depart_coord.latitude as 'orderDepartLat',\r\n"
				+ "order_dest_coord.longitude as 'orderDestLng',\r\n"
				+ "order_dest_coord.latitude as 'orderDestLat',\r\n" + "order_translation.text_" + userLocale
				+ " as 'orderStatus',\r\n" + "orders.status_id as 'orderStatusId',\r\n" + "`Users`.id as 'userId',\r\n"
				+ "`Users`.username as 'username',\r\n" + "`Users`.name as 'userName',\r\n"
				+ "`Users`.surname as 'userSurname',\r\n" + "`Users`.rating as 'userRating',\r\n"
				+ "user_roles.name as 'userRole',\r\n" + "Cars.id as 'carId',\r\n" + "Cars.plate as 'carPlate',\r\n"
				+ "Manufacturers.name as 'carManufacturer',\r\n" + "Models.name as 'carModel',\r\n"
				+ "Cars.price_multiplier as 'carPriceMult',\r\n" + "car_translation.text_" + userLocale
				+ " as 'carCategory',\r\n" + "Cars.passengerCount as 'carPassengerCount',\r\n"
				+ "car_coord.longitude as 'carLng',\r\n" + "car_coord.latitude as 'carLat',\r\n"
				+ "Drivers.id as 'driverId',\r\n" + "Drivers.name as 'driverName',\r\n"
				+ "Drivers.surname as 'driverSurname',\r\n" + "Drivers.rating as 'driverRating'\r\n " + "\r\n"
				+ "FROM orders\r\n "
				+ "JOIN Coordinates order_depart_coord ON order_depart_coord.id = orders.departure_coordinate_id \r\n"
				+ "JOIN Coordinates order_dest_coord ON order_dest_coord.id = orders.destination_coordinate_id \r\n"
				+ "JOIN order_status ON order_status.id = orders.status_id \r\n"
				+ "JOIN Translations order_translation ON order_translation.id = order_status.name_translations_id \r\n"
				+ "JOIN `Users` ON orders.user_id = `Users`.id \r\n"
				+ "JOIN user_roles ON user_roles.id = `Users`.role_id \r\n"
				+ "JOIN Driving ON Orders.driving_id = Driving.id \r\n" + "JOIN Cars ON Cars.id = Driving.car_id \r\n"
				+ "JOIN Manufacturers ON Cars.manufacturer_id = Manufacturers.id \r\n"
				+ "JOIN Models ON Cars.model_id = Models.id \r\n"
				+ "JOIN Translations car_translation ON Cars.category_translation_id = car_translation.id \r\n"
				+ "JOIN Coordinates car_coord ON Cars.coordinates_id = car_coord.id \r\n"
				+ "JOIN Drivers ON Driving.driver_id = Drivers.id \r\n";
		int startParameterIndex = 0;
		if (filter) {
			startParameterIndex++;
			if ("date".equalsIgnoreCase(filterBy)) {
				query += " WHERE DATE(dateOfOrder) = ?";
			} else if ("user".equalsIgnoreCase(filterBy)) {
				query += " WHERE `Users`.username = ?";
			}
		}
		query += "\r\n" + "LIMIT ?,? \r\n";
		Connection connection = getNewConnection();
		try (PreparedStatement getOrderIds = connection.prepareStatement(query)) {
			if (filter) {
				getOrderIds.setString(1, value);
			}
			getOrderIds.setInt(startParameterIndex + 1, skip);
			getOrderIds.setInt(startParameterIndex + 2, limit - skip);
			try (ResultSet rs = getOrderIds.executeQuery()) {
				while (rs.next()) {
					Coordinates routeDeparture = new Coordinates(rs.getString("orderDepartLng"),
							rs.getString("orderDepartLat"));
					Coordinates routeDestination = new Coordinates(rs.getString("orderDestLng"),
							rs.getString("orderDestLat"));
					Route route = Route.builder()
							.distance(rs.getFloat("orderDistance"))
							.time(rs.getInt("orderTime"))
							.departure(routeDeparture)
							.destination(routeDestination)
							.build();
					User user = User.builder().id(UUID.fromString(rs.getString("userId")))
							.username(rs.getString("username")).name(rs.getString("userName"))
							.surname(rs.getString("userSurname")).rating(rs.getFloat("userRating"))
							.role(Role.valueOf(rs.getString("userRole"))).build();
					Car car = Car.builder().id(rs.getInt("carId")).plate(rs.getString("carPlate"))
							.manufacturer(rs.getString("carManufacturer")).model(rs.getString("carModel"))
							.category(rs.getString("carCategory")).passengerCount(rs.getInt("carPassengerCount"))
							.priceMultiplier(rs.getFloat("carPriceMult"))
							.coordinates(new Coordinates(rs.getString("carLng"), rs.getString("carLat"))).build();
					Driver driver = Driver.builder().id(rs.getInt("driverId")).name(rs.getString("driverName"))
							.surname(rs.getString("driverSurname")).rating(rs.getFloat("driverRating")).build();
					Order order = Order.builder().id(rs.getInt("orderId")).price(rs.getFloat("orderPrice"))
							.timeToArrival(rs.getInt("orderTime"))
							.dateOfOrder(rs.getTimestamp("orderDate").toLocalDateTime())
							.statusid(rs.getInt("orderStatusId")).status(rs.getString("orderStatus")).route(route)
							.customer(user).car(car).driver(driver).build();
					result.add(order);
				}
				connection.commit();
			}
		} catch (SQLException e) {
			throw new IncorrectDateFormatException(e.getMessage()+"\r\nUse yyyy-MM-dd");
		} finally {
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return result;
	}

	public Optional<Integer> getOrderCountByUserAndStatus(UUID userid, int orderStatusId) {
		String query = "SELECT COUNT(id) as count FROM Orders WHERE user_id=? AND status_id=?";
		Connection connection = getNewConnection();
		try (PreparedStatement getOrderCount = connection.prepareStatement(query)) {
			getOrderCount.setString(1, userid.toString());
			getOrderCount.setInt(2, orderStatusId);
			try (ResultSet rs = getOrderCount.executeQuery()) {
				Optional<Integer> count = Optional.empty();
				while (rs.next()) {
					count = Optional.of(rs.getInt("count"));
				}
				connection.commit();
				return count;
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

	public Optional<Integer> getTotalOrderCountFiltered(String filterBy, String value) {
		String query = "SELECT COUNT(orders.id) as count FROM Orders";
		if ("date".equalsIgnoreCase(filterBy)) {
			query += " WHERE DATE(dateOfOrder) = ?";
		} else if ("user".equalsIgnoreCase(filterBy)) {
			query += " JOIN `Users` ON Orders.user_id = `Users`.id WHERE `Users`.username = ?";
		}
		Connection connection = getNewConnection();
		try (PreparedStatement getOrderCount = connection.prepareStatement(query)) {
			if (!filterBy.isBlank())
				getOrderCount.setString(1, value);
			try (ResultSet rs = getOrderCount.executeQuery()) {
				Optional<Integer> count = Optional.empty();
				while (rs.next()) {
					count = Optional.of(rs.getInt("count"));
				}
				connection.commit();
				return count;
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

}
