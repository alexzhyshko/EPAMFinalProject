package main.java.controller;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.dto.Car;
import main.java.dto.Coordinates;
import main.java.dto.Driver;
import main.java.dto.Order;
import main.java.dto.Route;
import main.java.dto.User;
import main.java.dto.request.RouteCreateRequest;
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
		if (userLocale == null) {
			resp.getWriter().append("User locale not set").flush();
			resp.setStatus(403);
			return;
		}
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
			car = carService.getCarByPlacesCountAndCategory(requestObj.numberOfPassengers, requestObj.carCategory,
					userLocale);
		} catch (Exception e) {
			if (anyCategory) {
				try {
					car = carService.getCarByPlacesCount(requestObj.numberOfPassengers, userLocale);
				} catch (NoSuitableCarFound noCarFoundExc) {
					noCarFoundExc.printStackTrace();
					resp.getWriter().append("Couldn't find a car to match passengers count").flush();
					resp.setStatus(404);
					return;
				}
			} else if (anyCountOfCars) {
				//TODO implement functionality to find a couple of cars to match order
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
		Order order = orderService.tryPlaceOrder(routeCreated, user, driver, car);
		Coordinates carDeparture = car.getCoordinates();
		Coordinates carDestination = departure;
		Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination);
		carService.setCarStatus(car.getId(), 2);
		int arrivalTime = carArrivalRoute.time;
		order.timeToArrival = arrivalTime;
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(order)).flush();
		resp.setStatus(201);
	}

	@Mapping(route = "/order/get/:pathVar/byUserId/:pathVar", requestType = RequestType.GET)
	public void getOrdersByUserId(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		if (userLocale == null) {
			resp.getWriter().append("User locale not set").flush();
			resp.setStatus(403);
			return;
		}
		String[] pathParts = req.getPathTranslated().replace("\\", "/").split("/");
		String type = pathParts[pathParts.length - 3];
		UUID userid = UUID.fromString(pathParts[pathParts.length - 1]);
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
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(result)).flush();
		resp.setStatus(200);
	}

	@Mapping(route = "/order/get/byId:arg", requestType = RequestType.GET)
	public void getOrderById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		if (userLocale == null) {
			resp.getWriter().append("User locale not set").flush();
			resp.setStatus(403);
			return;
		}
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
		if (userLocale == null) {
			resp.getWriter().append("User locale not set").flush();
			resp.setStatus(403);
			return;
		}
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
			resp.setStatus(200);
			return;
		}
		resp.getWriter().append("Error finishing your order").flush();
		resp.setStatus(500);
	}

}
