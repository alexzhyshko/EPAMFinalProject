package main.java.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import main.java.entity.Car;
import main.java.entity.Driver;
import main.java.entity.Order;
import main.java.entity.Route;
import main.java.entity.User;

public interface OrderRepository {

	Optional<Order> tryCreateOrder(Route route, User customer, Driver driver, Car car, float price);
	boolean finishOrder(int orderId);
	List<Order> getAllOrdersByStatusAndUser(UUID userid, int status, int skip, int limit, String userLocale);
	Optional<Order> getOrderById(int orderId, String userLocale);
	List<Order> getAllOrders(String userLocale, String filterBy, String value, int skip, int limit, boolean filter);
	Optional<Integer> getOrderCountByUserAndStatus(UUID userid, int orderStatusId);
	Optional<Integer> getTotalOrderCountFiltered(String filterBy, String value);
	
	
	
}
