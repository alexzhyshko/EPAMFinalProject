package main.java.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;
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

	@Mapping(route = "/order/create:arg:arg", requestType = RequestType.POST)
	public void onOrderCreateRequestReceived(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		boolean anyCategory = false;
		try {
			anyCategory = Boolean.valueOf(req.getParameter("anyCategory"));
		} catch (Exception e) {
			resp.getWriter().append("Incorrect parameter anyCategory").flush();
			resp.setStatus(403);
			return;
		}
		boolean anyCountOfCars = false;
		try {
			anyCountOfCars = Boolean.valueOf(req.getParameter("anyCountOfCars"));
		} catch (Exception e) {
			resp.getWriter().append("Incorrect parameter anyCountOfCars").flush();
			resp.setStatus(403);
			return;
		}
		String authTokenHeader = req.getHeader("Authorization");
		String jwt = authTokenHeader.substring(7);
		User user = userService.getUserByToken(jwt);
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		RouteCreateRequest requestObj = gson.fromJson(body, RouteCreateRequest.class);
		Coordinates departure = new Coordinates(requestObj.departureLongitude, requestObj.departureLatitude);
		Coordinates destination = new Coordinates(requestObj.destinationLongitude, requestObj.destinationLatitude);
		Route routeCreated = routeService.tryGetRoute(departure, destination);
		Car car = null;
		try {
			car = carService.getNearestCarByPlacesCountAndCategory(requestObj.numberOfPassengers,
					requestObj.carCategory, userLocale, departure);
		} catch (Exception e) {
			e.printStackTrace();
			if (anyCategory) {
				try {
					car = carService.getNearestCarByPlacesCount(requestObj.numberOfPassengers, userLocale, departure);
				} catch (NoSuitableCarFound noCarFoundExc) {
					noCarFoundExc.printStackTrace();
					resp.getWriter().append(noCarFoundExc.getMessage()).flush();
					resp.setStatus(404);
					return;
				}
			} else if (anyCountOfCars) {
				// TODO implement functionality to find a couple of cars to match order
				resp.getWriter().append("Couldn't find a car to match passengers count").flush();
				resp.setStatus(404);
				return;
			}
		}
		if (car == null) {
			resp.getWriter().append("Could not find a suitable car").flush();
			resp.setStatus(404);
			return;
		}
		Driver driver = driverService.getDriverByCar(car);
		Order order = orderService.tryPlaceOrder(routeCreated, user, driver, car, userLocale);
		Coordinates carDeparture = car.getCoordinates();
		Coordinates carDestination = departure;
		Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination);
		carService.setCarStatus(car.getId(), 2);
		int arrivalTime = carArrivalRoute.time;
		order.timeToArrival = arrivalTime;
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(order)).flush();
		resp.setStatus(200);
	}

	@Mapping(route = "/order/getRouteDetails", requestType = RequestType.POST)
	public void getRouteDetails(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		List<RouteDetails> response = new ArrayList<>();
		String userLocale = req.getHeader("User_Locale");
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		RouteCreateRequest requestObj = gson.fromJson(body, RouteCreateRequest.class);
		Coordinates departure = new Coordinates(requestObj.departureLongitude, requestObj.departureLatitude);
		Coordinates destination = new Coordinates(requestObj.destinationLongitude, requestObj.destinationLatitude);
		Route routeCreated = routeService.tryGetRoute(departure, destination);
		for (CarCategory category : CarCategory.values()) {
			Car car = null;
			try {
				car = carService.getNearestCarByPlacesCountAndCategory(requestObj.numberOfPassengers,
						category.toString(), userLocale, departure);
			} catch (NoSuitableCarFound e) {
				continue;
			}
			if (car != null) {
				RouteDetails details = new RouteDetails();
				Coordinates carDeparture = car.getCoordinates();
				Coordinates carDestination = departure;
				Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination);
				details.price = orderService.getRouteRawPrice(routeCreated, car);
				details.arrivalTime = carArrivalRoute.time;
				details.categoryLocaleName = carService.getCategoryByLocale(category, userLocale);
				response.add(details);
			}
		}
		String responseJson = gson.toJson(response);
		resp.getWriter().append(responseJson).flush();
		resp.setStatus(200);
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
		List<Order> result = null;
		if ("all".equals(type)) {
			result = orderService.getAllOrdersByUser(userid, userLocale);
		} else if ("finished".equals(type)) {
			result = orderService.getFinishedOrdersByUser(userid, userLocale);
		} else if ("active".equals(type)) {
			result = orderService.getActiveOrdersByUser(userid, userLocale);
		} else {
			resp.getWriter().append("Incorrect path variable " + type).flush();
			resp.setStatus(403);
			return;
		}
		for (Order order : result.stream().filter(e -> e.statusid == 1).collect(Collectors.toList())) {
			Car car = order.car;
			Route route = order.route;
			Coordinates carPosition = car.getCoordinates();
			Coordinates clientDeparture = route.departure;
			Route carArrivalRoute = routeService.tryGetRoute(carPosition, clientDeparture);
			order.timeToArrival = carArrivalRoute.time;
		}
		int elementsPerPage = 4;
		UserOrdersResponse response = new UserOrdersResponse();
		response.numberOfPages = result.size() / elementsPerPage;
		if (result.size() % elementsPerPage != 0) {
			response.numberOfPages++;
		}
		response.orders = result.stream().limit(page * elementsPerPage + elementsPerPage).skip(page * elementsPerPage)
				.collect(Collectors.toList());
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(response)).flush();
		resp.setStatus(200);
	}

	
	@Mapping(route = "/order/get/byId:arg", requestType = RequestType.GET)
	public void getOrderById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		int orderId = -1;
		try {
			orderId = Integer.parseInt(req.getParameter("orderId"));
		} catch (NumberFormatException | NullPointerException e) {
			resp.getWriter().append("Incorrect orderId").flush();
			resp.setStatus(403);
			return;
		}
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(orderService.getOrderById(orderId, userLocale))).flush();
		resp.setStatus(403);
	}

	@Mapping(route = "/order/finish:arg", requestType = RequestType.GET)
	public void onOrderFinishRequestREceived(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		int orderId = -1;
		try {
			orderId = Integer.parseInt(req.getParameter("orderId"));
		} catch (NumberFormatException | NullPointerException e) {
			resp.getWriter().append("Incorrect orderId").flush();
			resp.setStatus(403);
			return;
		}
		boolean success = orderService.finishOrder(orderId);
		Car car = carService.getCarByOrderId(orderId, userLocale);
		if (success) {
			carService.setCarStatus(car.getId(), 1);
			resp.getWriter().append("OK").flush();
			resp.setStatus(HttpStatus.SC_OK);
			return;
		}
		resp.getWriter().append("Error finishing your order").flush();
		resp.setStatus(500);
	}

}
