package test.java.mock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import main.java.entity.Car;
import main.java.entity.Driver;
import main.java.entity.Order;
import main.java.entity.Route;
import main.java.entity.User;
import main.java.repository.OrderRepository;

public class OrderRepositoryMock implements OrderRepository{

	
	private Map<Integer, Order> orders = new HashMap<>();
	
	@Override
	public Optional<Order> tryCreateOrder(Route route, User customer, Driver driver, Car car, float price) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean finishOrder(int orderId) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<Order> getAllOrdersByStatusAndUser(UUID userid, int status, int skip, int limit, String userLocale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Order> getOrderById(int orderId, String userLocale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Order> getAllOrders(String userLocale, String filterBy, String value, int skip, int limit,
			boolean filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Integer> getOrderCountByUserAndStatus(UUID userid, int orderStatusId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<Integer> getTotalOrderCountFiltered(String filterBy, String value) {
		// TODO Auto-generated method stub
		return null;
	}

}
