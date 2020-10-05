package main.java.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
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
import main.java.exception.NoSuitableCarFound;
import main.java.service.CarService;
import main.java.service.DriverService;
import main.java.service.LocalizationService;
import main.java.service.OrderService;
import main.java.service.RouteService;
import main.java.service.UserService;

@Component
@RestController
public class OrderController {

	private Gson gson = new Gson();

	@Inject
	private RouteService routeService;

	@Inject
	private OrderService orderService;

	@Inject
	private UserService userService;

	@Inject
	private CarService carService;

	@Inject
	private DriverService driverService;

	@Inject
	private LocalizationService localizator;
	
	@Mapping(route = "/order/create:arg:arg", requestType = RequestType.POST)
	public void onOrderCreateRequestReceived(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		boolean anyCategory = false;
		try {
			anyCategory = Boolean.valueOf(req.getParameter("anyCategory"));
		} catch (Exception e) {
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "incorrectParameter")).flush();
			return;
		}
		boolean anyCountOfCars = false;
		try {
			anyCountOfCars = Boolean.valueOf(req.getParameter("anyCountOfCars"));
		} catch (Exception e) {
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "incorrectParameter")).flush();
			return;
		}
		try {
			String authTokenHeader = req.getHeader("Authorization");
			String jwt = authTokenHeader.substring(7);
			User user = userService.getUserByToken(jwt);
			String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
			RouteCreateRequest requestObj = gson.fromJson(body, RouteCreateRequest.class);
			Coordinates departure = new Coordinates(requestObj.getDepartureLongitude(), requestObj.getDepartureLatitude());
			Coordinates destination = new Coordinates(requestObj.getDestinationLongitude(), requestObj.getDestinationLatitude());
			Route routeCreated = routeService.tryGetRoute(departure, destination)
					.orElseThrow(() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
			Car car = null;
			try {
				car = carService.getNearestCarByPlacesCountAndCategory(requestObj.getNumberOfPassengers(),
						requestObj.getCarCategory(), userLocale, departure);
			} catch (Exception e) {
				e.printStackTrace();
				if (anyCategory) {
					try {
						car = carService.getNearestCarByPlacesCount(requestObj.getNumberOfPassengers(), userLocale,
								departure);
					} catch (NoSuitableCarFound noCarFoundExc) {
						noCarFoundExc.printStackTrace();
						resp.setStatus(HttpStatus.SC_NOT_FOUND);
						resp.getWriter().append(noCarFoundExc.getMessage()).flush();
						return;
					}
				} else if (anyCountOfCars) {
					// TODO implement functionality to find a couple of cars to match order
					resp.setStatus(HttpStatus.SC_NOT_FOUND);
					resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "couldNotFindMatchCarByPlacesAndCategory")).flush();
					return;
				}
			}
			if (car == null) {
				resp.setStatus(HttpStatus.SC_NOT_FOUND);
				resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "couldNotFindSuitableCar")).flush();
				return;
			}
			Driver driver = driverService.getDriverByCar(car);
			Order order = orderService.tryPlaceOrder(routeCreated, user, driver, car, userLocale);
			Coordinates carDeparture = car.getCoordinates();
			Coordinates carDestination = departure;
			Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination)
					.orElseThrow(() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
			carService.setCarStatus(car.getId(), 2);
			int arrivalTime = carArrivalRoute.time;
			order.setTimeToArrival(arrivalTime);
			resp.setContentType("application/json");
			resp.setStatus(HttpStatus.SC_OK);
			resp.getWriter().append(gson.toJson(order)).flush();
		} catch (NullPointerException e) {
			resp.setStatus(HttpStatus.SC_NOT_FOUND);
			resp.getWriter().append(e.getMessage()).flush();
		}
	}

	@Mapping(route = "/order/getRouteDetails", requestType = RequestType.POST)
	public void getRouteDetails(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		List<RouteDetails> response = new ArrayList<>();
		String userLocale = req.getHeader("User_Locale");
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		RouteCreateRequest requestObj = gson.fromJson(body, RouteCreateRequest.class);
		try {
			Coordinates departure = new Coordinates(requestObj.getDepartureLongitude(), requestObj.getDepartureLatitude());
			Coordinates destination = new Coordinates(requestObj.getDestinationLongitude(), requestObj.getDestinationLatitude());
			Route routeCreated = routeService.tryGetRoute(departure, destination)
					.orElseThrow(() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
			for (CarCategory category : CarCategory.values()) {
				Car car = null;
				try {
					car = carService.getNearestCarByPlacesCountAndCategory(requestObj.getNumberOfPassengers(),
							category.toString(), userLocale, departure);
				} catch (NoSuitableCarFound e) {
					continue;
				}

				if (car != null) {
					
					Coordinates carDeparture = car.getCoordinates();
					Coordinates carDestination = departure;
					Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination)
							.orElseThrow(() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
					RouteDetails details = RouteDetails.builder()
							.price(orderService.getRouteRawPrice(routeCreated, car))
							.arrivalTime(carArrivalRoute.time)
							.categoryLocaleName(carService.getCategoryByLocale(category, userLocale))
							.build();
					response.add(details);
				}

			}
		} catch (NullPointerException e) {
			
			resp.setStatus(HttpStatus.SC_NOT_FOUND);
			resp.getWriter().append(e.getMessage()).flush();
			return;
		}
		if(response.isEmpty()) {
			resp.setContentType("text/plain");
			resp.setStatus(HttpStatus.SC_NOT_FOUND);
			System.out.println(localizator.getPropertyByLocale(userLocale, "couldNotFindMatchCarByPlaces"));
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "couldNotFindMatchCarByPlaces"));
			return;
		}
		resp.setContentType("application/json");
		String responseJson = gson.toJson(response);
		resp.setStatus(HttpStatus.SC_OK);
		resp.getWriter().append(responseJson).flush();
	}

	@Mapping(route = "/order/get/byUserId:arg:arg:arg", requestType = RequestType.GET)
	public void getOrdersByUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		String type = req.getParameter("type");
		UUID userid = UUID.fromString(req.getParameter("userId"));
		int page = 0;
		try {
			page = Integer.parseInt(req.getParameter("page"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		int elementsPerPage = 4;
		int totalNumberOfOrders = -1;
		List<Order> result = null;
		if ("all".equals(type)) {
			result = orderService.getAllOrdersByUser(userid, userLocale, page * elementsPerPage , page * elementsPerPage + elementsPerPage);
			totalNumberOfOrders = orderService.getTotalOrderCountByUser(userid);
		} else if ("finished".equals(type)) {
			result = orderService.getFinishedOrdersByUser(userid, userLocale, page * elementsPerPage , page * elementsPerPage + elementsPerPage);
			totalNumberOfOrders = orderService.getFinishedOrderCountByUser(userid);
		} else if ("active".equals(type)) {
			result = orderService.getActiveOrdersByUser(userid, userLocale, page * elementsPerPage , page * elementsPerPage + elementsPerPage);
			totalNumberOfOrders = orderService.getActiveOrderCountByUser(userid);
		} else {
			resp.setContentType("text/plain");
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "incorrectPathVariable")).flush();
			return;
		}
		try {
			for (Order order : result.stream().filter(e -> e.getStatusid() == 1).collect(Collectors.toList())) {
				Car car = order.getCar();
				Route route = order.getRoute();
				Coordinates carPosition = car.getCoordinates();
				Coordinates clientDeparture = route.departure;
				Route carArrivalRoute = routeService.tryGetRoute(carPosition, clientDeparture)
						.orElseThrow(() -> new NullPointerException(localizator.getPropertyByLocale(userLocale, "couldNotGetRoute")));
				order.setTimeToArrival(carArrivalRoute.time);
			}
		} catch (NullPointerException e) {
			resp.setContentType("text/plain");
			resp.setStatus(HttpStatus.SC_NOT_FOUND);
			resp.getWriter().append(e.getMessage()).flush();
			return;
		}
		
		int numberOfPages = totalNumberOfOrders / elementsPerPage;
		if (totalNumberOfOrders % elementsPerPage != 0) {
			numberOfPages++;
		}
		UserOrdersResponse response = UserOrdersResponse.builder()
				.numberOfPages(numberOfPages)
				.orders(result)
				.build();
		resp.setContentType("application/json");
		resp.setStatus(HttpStatus.SC_OK);
		resp.getWriter().append(gson.toJson(response)).flush();
	}

	@Mapping(route = "/order/get/byId:arg", requestType = RequestType.GET)
	public void getOrderById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		int orderId = -1;
		try {
			orderId = Integer.parseInt(req.getParameter("orderId"));
		} catch (NumberFormatException | NullPointerException e) {
			resp.setContentType("text/plain");
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "incorrectOrderId")).flush();
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			return;
		}
		resp.setContentType("application/json");
		resp.setStatus(HttpStatus.SC_OK);
		resp.getWriter().append(gson.toJson(orderService.getOrderById(orderId, userLocale))).flush();
	}

	@Mapping(route = "/order/finish:arg", requestType = RequestType.GET)
	public void onOrderFinishRequestREceived(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		int orderId = -1;
		try {
			orderId = Integer.parseInt(req.getParameter("orderId"));
		} catch (NumberFormatException | NullPointerException e) {
			resp.setContentType("text/plain");
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "incorrectOrderId")).flush();
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			return;
		}
		boolean success = orderService.finishOrder(orderId);
		Car car = carService.getCarByOrderId(orderId, userLocale);
		if (success) {
			carService.setCarStatus(car.getId(), 1);
			resp.setContentType("text/plain");
			resp.setStatus(HttpStatus.SC_OK);
			resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "orderFinished")).flush();
			return;
		}
		resp.setContentType("text/plain");
		resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
		resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "errorFinishingOrder")).flush();
	}

}
