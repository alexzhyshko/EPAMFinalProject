package main.java.service;

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
	private OrderRepository orderRepoitory;
	
	private static final int STANDART_FEE_PER_KILOMETER = 20;
	
	public Order tryPlaceOrder(Route route, User customer, Driver driver, Car car) {
		int price = Math.round(route.distance*car.getPriceMultiplier())*STANDART_FEE_PER_KILOMETER;
		return orderRepoitory.tryCreateOrder(route, customer, driver, car, price);
	}
	
	
}
