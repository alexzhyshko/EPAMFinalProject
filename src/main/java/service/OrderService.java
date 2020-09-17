package main.java.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.dto.Car;
import main.java.dto.Driver;
import main.java.dto.Order;
import main.java.dto.Route;
import main.java.dto.User;
import main.java.repository.OrderRepository;

@Component
public class OrderService {

	@Inject
	private OrderRepository orderRepository;
	
	@Inject
	private UserService userService;
	
	private static final int STANDART_FEE_PER_KILOMETER = 20;
	
	public Order tryPlaceOrder(Route route, User customer, Driver driver, Car car) {
		int price = Math.round(route.distance*car.getPriceMultiplier()*STANDART_FEE_PER_KILOMETER);
		return orderRepository.tryCreateOrder(route, customer, driver, car, price);
	}
	
	public boolean finishOrder(int orderId) {
		return orderRepository.finishOrder(orderId);
	}
	
	public List<Order> getAllOrdersByUser(UUID userid, String userLocale){
		List<Order> result = orderRepository.getAllOrdersByStatusAndUser(userid, 1, userLocale);
		result.addAll(orderRepository.getAllOrdersByStatusAndUser(userid, 2, userLocale));
		return result;
	}
	
	public List<Order> getFinishedOrdersByUser(UUID userid, String userLocale){
		return orderRepository.getAllOrdersByStatusAndUser(userid, 2, userLocale);
	}
	
	public List<Order> getActiveOrdersByUser(UUID userid, String userLocale){
		return orderRepository.getAllOrdersByStatusAndUser(userid, 1, userLocale);
	}
	
	public Order getOrderById(int orderid, String userLocale) {
		return orderRepository.getOrderById(orderid, userLocale);
	}
	
	public Map<UUID, List<Order>> getAllOrders(String userLocale){
		HashMap<UUID, List<Order>> result = new HashMap<>();
		List<User> users = userService.getAllUsers();
		for(User user : users) {
			List<Order> userOrders = getAllOrdersByUser(user.getId(), userLocale);
			result.put(user.getId(), userOrders);
		}
		return result;
	}
	
}
