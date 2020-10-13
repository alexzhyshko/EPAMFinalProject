package main.java.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import main.java.dto.PaginationFilteringSortingDTO;
import main.java.dto.request.RouteCreateRequest;
import main.java.dto.response.RouteDetails;
import main.java.dto.response.UserOrdersResponse;
import main.java.entity.Car;
import main.java.entity.CarCategory;
import main.java.entity.Coordinates;
import main.java.entity.Driver;
import main.java.entity.Order;
import main.java.entity.Route;
import main.java.entity.User;
import main.java.exception.IncorrectParameterException;
import main.java.exception.NoSuitableCarFound;
import main.java.exception.RouteNotCreatedException;
import main.java.repository.OrderRepository;

@Component
public class OrderService {

	@Inject("OrderRepositoryImpl")
	public OrderRepository orderRepository;

	@Inject
	private UserService userService;

	@Inject
	private RouteService routeService;

	@Inject
	private LocalizationService localizator;

	@Inject
	private CarService carService;

	@Inject
	private DriverService driverService;

	private static final int STANDART_FEE_PER_KILOMETER = 5;
	private static final int BASE_RIDE_PRICE = 25;
	private static final int MINIMAL_RIDE_PRICE = 40;
	private static final int ELEMENTS_PER_USER_PAGE = 4;
	private static final int ELEMENTS_PER_ADMIN_PAGE = 15;
	
	public Order createOrder(String userLocale, boolean anyCategory, boolean anyCountOfCars, String jwt,
			RouteCreateRequest requestObj) {
		User user = userService.getUserByToken(jwt);
		Coordinates departureCoordinates = buildDepartureCoordinateFromDto(requestObj);
		Coordinates destinationCoordinates = buildDestinationCoordinateFromDto(requestObj);
		Route routeCreated = routeService.tryGetRoute(departureCoordinates, destinationCoordinates).orElseThrow(
				() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
		Car car = getCarByCategoryAndPlacesCount(userLocale, requestObj, anyCategory, anyCountOfCars,
				departureCoordinates)
						.orElseThrow(() -> new NoSuitableCarFound(
								localizator.getPropertyByLocale(userLocale, "couldNotFindSuitableCar")));
		Driver driver = driverService.getDriverByCar(car);
		Order order = tryPlaceOrder(routeCreated, user, driver, car, userLocale);
		Coordinates carDeparture = car.getCoordinates();
		Coordinates carDestination = departureCoordinates;
		Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination).orElseThrow(
				() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
		carService.setCarStatus(car.getId(), 2);
		int arrivalTime = carArrivalRoute.getTime();
		order.setTimeToArrival(arrivalTime);
		return order;
	}

	private Optional<Car> getCarByCategoryAndPlacesCount(String userLocale, RouteCreateRequest requestObj,
			boolean anyCategory, boolean anyCountOfCars, Coordinates departureCoordinates) {
		Optional<Car> car = Optional.empty();
		try {
			car = Optional.of(carService.getNearestCarByPlacesCountAndCategory(requestObj.getNumberOfPassengers(),
					requestObj.getCarCategory(), userLocale, departureCoordinates));
		} catch (Exception e) {
			if (anyCategory) {
				car = Optional.of(carService.getNearestCarByPlacesCount(requestObj.getNumberOfPassengers(), userLocale,
						departureCoordinates));
			} else if (anyCountOfCars) {
				throw new UnsupportedOperationException(
						localizator.getPropertyByLocale(userLocale, "couldNotFindMatchCarByPlacesAndCategory"));
			}
		}
		return car;
	}

	private Coordinates buildDestinationCoordinateFromDto(RouteCreateRequest requestObj) {
		return new Coordinates(requestObj.getDepartureLongitude(), requestObj.getDepartureLatitude());
	}

	private Coordinates buildDepartureCoordinateFromDto(RouteCreateRequest requestObj) {
		return new Coordinates(requestObj.getDestinationLongitude(), requestObj.getDestinationLatitude());
	}

	public Order tryPlaceOrder(Route route, User customer, Driver driver, Car car, String userLocale) {
		int price = this.getRouteRawPrice(route, car);
		List<Order> userPreviousOrders = getAllOrdersByUser(customer.getId(), userLocale, 0, Integer.MAX_VALUE);
		int discount = getLoyaltyDiscount(userPreviousOrders);
		price -= discount;
		return orderRepository.tryCreateOrder(route, customer, driver, car, price)
				.orElseThrow(() -> new NullPointerException("Could not place order"));
	}

	public Optional<List<RouteDetails>> getRouteDetails(RouteCreateRequest requestObj, String userLocale) {
		Coordinates departureCoordinates = buildDepartureCoordinateFromDto(requestObj);
		Coordinates destinationCoordinates = buildDestinationCoordinateFromDto(requestObj);
		Route routeCreated = routeService.tryGetRoute(departureCoordinates, destinationCoordinates).orElseThrow(
				() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
		List<RouteDetails> result = getNearestCarByEveryCategory(departureCoordinates, requestObj.getNumberOfPassengers(), routeCreated, userLocale);
		if (result.isEmpty()) {
			return Optional.empty();
		}
		return Optional.of(result);
	}

	private List<RouteDetails> getNearestCarByEveryCategory(Coordinates departureCoordinates, int numberOfPassengers, Route routeCreated,
			String userLocale) {
		List<RouteDetails> result = new ArrayList<>();
		for (CarCategory category : CarCategory.values()) {
			Car car = null;
			try {
				car = carService.getNearestCarByPlacesCountAndCategory(numberOfPassengers,
						category.toString(), userLocale, departureCoordinates);
			} catch (NoSuitableCarFound e) {
				continue;
			}
			if (car != null) {
				int arrivalTime = getArrivalTime(car.getCoordinates(), departureCoordinates, userLocale);
				RouteDetails details = RouteDetails.builder().price(getRouteRawPrice(routeCreated, car))
						.arrivalTime(arrivalTime)
						.categoryLocaleName(carService.getCategoryByLocale(category, userLocale)).build();
				result.add(details);
			}
		}
		return result;
	}

	private int getArrivalTime(Coordinates carCoordinates, Coordinates departureCoordinates, String userLocale) {
		Coordinates carDeparture = carCoordinates;
		Coordinates carDestination = departureCoordinates;
		Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination)
				.orElseThrow(() -> new NullPointerException(
						localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
		return carArrivalRoute.getTime();
	}

	public UserOrdersResponse getOrdersByUserId(String userLocale, String type, UUID userid, int page) {
		List<Order> result = null;
		result = getNeededDataByType(type, userid, userLocale, page * ELEMENTS_PER_USER_PAGE,
				page * ELEMENTS_PER_USER_PAGE + ELEMENTS_PER_USER_PAGE);
		try {
			result.stream().filter(order -> order.getStatusid() == 1).forEach(order->{
				Car car = order.getCar();
				Route route = order.getRoute();
				Coordinates carPosition = car.getCoordinates();
				Coordinates clientDeparture = route.getDeparture();
				int arrivalTime = getArrivalTime(carPosition, clientDeparture, userLocale);
				order.setTimeToArrival(arrivalTime);
			});
		} catch (NullPointerException e) {
			throw new RouteNotCreatedException(e.getMessage());
		}
		int numberOfPages = calculateNumberOfPagesByType(type, userid, userLocale);
		return UserOrdersResponse.builder().numberOfPages(numberOfPages).orders(result).build();
	}

	private int calculateNumberOfPagesByType(String type, UUID userid, String userLocale) {
		int totalNumberOfOrders = getNeededOrdersCountByType(type, userid, userLocale);
		int numberOfPages = totalNumberOfOrders / ELEMENTS_PER_USER_PAGE;
		if (totalNumberOfOrders % ELEMENTS_PER_USER_PAGE != 0) {
			numberOfPages++;
		}
		return numberOfPages;
	}

	private List<Order> getNeededDataByType(String type, UUID userid, String userLocale, int skip, int limit) {
		if ("all".equals(type)) {
			return getAllOrdersByUser(userid, userLocale, skip, limit);
		}
		if ("finished".equals(type)) {
			return getFinishedOrdersByUser(userid, userLocale, skip, limit);
		}
		if ("active".equals(type)) {
			return getActiveOrdersByUser(userid, userLocale, skip, limit);
		}
		throw new IncorrectParameterException(localizator.getPropertyByLocale(userLocale, "incorrectPathVariable"));
	}

	private int getNeededOrdersCountByType(String type, UUID userid, String userLocale) {
		if ("all".equals(type)) {
			return getTotalOrderCountByUser(userid);
		}
		if ("finished".equals(type)) {
			return getFinishedOrderCountByUser(userid);
		}
		if ("active".equals(type)) {
			return getActiveOrderCountByUser(userid);
		}
		throw new IncorrectParameterException(localizator.getPropertyByLocale(userLocale, "incorrectPathVariable"));
	}

	public Optional<UserOrdersResponse> getAllOrdersSortedFiltered(PaginationFilteringSortingDTO dto,
			String userLocale) {
		List<Order> allOrders = getOrdersByFilterPaginated(dto, userLocale);
		int numberOfPages = calculatePageCount(dto.getFilterBy(), dto.getValue());
		if (numberOfPages == 0) {
			return Optional.empty();
		}
		if (dto.isSort()) {
			Comparator<Order> comparator = getComparatorBySortType(dto.getSortBy(), userLocale);
			allOrders.sort(dto.getOrder().equalsIgnoreCase("asc") ? comparator : comparator.reversed());
		}
		return Optional.of(UserOrdersResponse.builder().numberOfPages(numberOfPages).orders(allOrders).build());

	}

	private List<Order> getOrdersByFilterPaginated(PaginationFilteringSortingDTO dto, String userLocale) {
		int skip = dto.getPage() * ELEMENTS_PER_ADMIN_PAGE;
		int limit = dto.getPage() * ELEMENTS_PER_ADMIN_PAGE + ELEMENTS_PER_ADMIN_PAGE;
		if (dto.isFilter()) {
			return getAllOrdersFiltered(userLocale, dto.getFilterBy(), dto.getValue(), skip, limit);
		} else {
			return getAllOrders(userLocale, skip, limit);
		}
	}

	private Comparator<Order> getComparatorBySortType(String sortBy, String userLocale) {
		if ("dateOfOrder".equals(sortBy)) {
			return (order1, order2) -> order1.getDateOfOrder().compareTo(order2.getDateOfOrder());
		} else if ("price".equals(sortBy)) {
			return (order1, order2) -> Float.compare(order1.getPrice(), order2.getPrice());
		} else
			throw new IllegalArgumentException(
					localizator.getPropertyByLocale(userLocale, "notSupportedSortOption"));
	}

	private int calculatePageCount(String filterBy, String value) {
		int totalNumberOfOrders = getTotalOrderCount(filterBy, value);
		int numberOfPages = totalNumberOfOrders / ELEMENTS_PER_ADMIN_PAGE;
		if (totalNumberOfOrders % ELEMENTS_PER_ADMIN_PAGE != 0) {
			numberOfPages++;
		}
		return numberOfPages;
	}

	private int getRouteRawPrice(Route route, Car car) {
		int price = Math.round(route.getDistance() * car.getPriceMultiplier() * STANDART_FEE_PER_KILOMETER)
				+ BASE_RIDE_PRICE;
		return price < MINIMAL_RIDE_PRICE ? MINIMAL_RIDE_PRICE : price;
	}

	private int getLoyaltyDiscount(List<Order> userPreviousOrders) {
		long totalOrderSum = Math.round(userPreviousOrders.stream().mapToDouble(Order::getPrice).sum());
		return Math.round(totalOrderSum * 0.01f);
	}

	public boolean finishOrder(int orderId, String userLocale) {
		boolean success = orderRepository.finishOrder(orderId);
		Car car = carService.getCarByOrderId(orderId, userLocale);
		if (success)
			carService.setCarStatus(car.getId(), 1);
		return success;
	}

	private List<Order> getAllOrdersByUser(UUID userid, String userLocale, int skip, int limit) {
		List<Order> result = orderRepository.getAllOrdersByStatusAndUser(userid, 1, skip, limit, userLocale);
		result.addAll(orderRepository.getAllOrdersByStatusAndUser(userid, 2, skip, limit, userLocale));
		return result;
	}

	private List<Order> getFinishedOrdersByUser(UUID userid, String userLocale, int skip, int limit) {
		return orderRepository.getAllOrdersByStatusAndUser(userid, 2, skip, limit, userLocale);
	}

	private List<Order> getActiveOrdersByUser(UUID userid, String userLocale, int skip, int limit) {
		return orderRepository.getAllOrdersByStatusAndUser(userid, 1, skip, limit, userLocale);
	}

	public Order getOrderById(int orderid, String userLocale) {
		return orderRepository.getOrderById(orderid, userLocale)
				.orElseThrow(() -> new NullPointerException("No order found by id"));
	}

	private List<Order> getAllOrdersFiltered(String userLocale, String filterBy, String value, int skip, int limit) {
		return orderRepository.getAllOrders(userLocale, filterBy, value, skip, limit, true);
	}

	private List<Order> getAllOrders(String userLocale, int skip, int limit) {
		return orderRepository.getAllOrders(userLocale, "", "", skip, limit, false);
	}

	private int getTotalOrderCountByUser(UUID userid) {
		return getFinishedOrderCountByUser(userid) + getActiveOrderCountByUser(userid);
	}

	private int getActiveOrderCountByUser(UUID userid) {
		return this.orderRepository.getOrderCountByUserAndStatus(userid, 1)
				.orElseThrow(() -> new NullPointerException("Could not get active order count by user"));
	}

	private int getFinishedOrderCountByUser(UUID userid) {
		return this.orderRepository.getOrderCountByUserAndStatus(userid, 2)
				.orElseThrow(() -> new NullPointerException("Could not get finished order count by user"));
	}

	private int getTotalOrderCount(String filterBy, String value) {
		return this.orderRepository.getTotalOrderCountFiltered(filterBy, value)
				.orElseThrow(() -> new NullPointerException("Could not get total order count"));
	}

}
