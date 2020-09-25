package main.java.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import main.java.entity.Car;
import main.java.entity.Driver;
import main.java.entity.Order;
import main.java.entity.Route;
import main.java.entity.User;
import main.java.repository.OrderRepository;

@Component
public class OrderService {

	@Inject
	private OrderRepository orderRepository;
	
	@Inject
	private UserService userService;
	
	private static final int STANDART_FEE_PER_KILOMETER = 5;
	private static final int BASE_RIDE_PRICE = 25;
	private static final int MINIMAL_RIDE_PRICE = 40;
	
	
	public Order tryPlaceOrder(Route route, User customer, Driver driver, Car car, String userLocale) {
		int price = this.getRouteRawPrice(route, car);
		List<Order> userPreviousOrders = getAllOrdersByUser(customer.getId(), userLocale);
		int discount = getLoyaltyDiscount(userPreviousOrders);
		price -= discount;
		return orderRepository.tryCreateOrder(route, customer, driver, car, price).orElseThrow(()-> new NullPointerException("Could not place order"));
	}

	public int getRouteRawPrice(Route route, Car car) {
		int price = Math.round(route.distance*car.getPriceMultiplier()*STANDART_FEE_PER_KILOMETER)+BASE_RIDE_PRICE;
		return price<MINIMAL_RIDE_PRICE?MINIMAL_RIDE_PRICE:price; 
	}
	
	public int getLoyaltyDiscount(List<Order> userPreviousOrders) {
		long totalOrderSum = Math.round(userPreviousOrders.stream().mapToDouble(order->order.price).sum());
		return Math.round(totalOrderSum*0.01f);
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
		return orderRepository.getOrderById(orderid, userLocale).orElseThrow(()-> new NullPointerException("No order found by id"));
	}
	
	public List<Order> getAllOrders(String userLocale){
		List<Order> result = new ArrayList<>();
		List<User> users = userService.getAllUsers();
		for(User user : users) {
			List<Order> userOrders = getAllOrdersByUser(user.getId(), userLocale);
			result.addAll(userOrders);
		}
		return result;
	}
	
}
