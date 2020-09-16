package main.java.controller;

import java.io.IOException;
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
	
	@Mapping(route="/order/create", requestType=RequestType.POST)
	public void onRouteCreateRequestReceived(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		if(userLocale == null) {
			resp.getWriter().append("User locale not set").flush();
			resp.setStatus(403);
			return;
		}
		String authTokenHeader = req.getHeader("Authorization");
		String jwt = authTokenHeader.substring(7);
		String body = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
		RouteCreateRequest requestObj = gson.fromJson(body, RouteCreateRequest.class);
		Coordinates departure = new Coordinates(requestObj.departureLongitude, requestObj.departureLatitude);
		Coordinates destination = new Coordinates(requestObj.destinationLongitude, requestObj.destinationLatitude);
		Route routeCreated = routeService.tryGetRoute(departure, destination);
		User user = userService.getUserByToken(jwt);
		Car car = carService.getCarByPlacesCountAndCategory(requestObj.numberOfPassengers, requestObj.carCategory, userLocale);
		Driver driver = driverService.getDriverByCar(car);
		Order order = orderService.tryPlaceOrder(routeCreated, user, driver, car);
		Coordinates carDeparture = car.getCoordinates();
		Coordinates carDestination = departure;
		Route carArrivalRoute = routeService.tryGetRoute(carDeparture, carDestination);
		int arrivalTime = carArrivalRoute.time;
		order.timeToArrival = arrivalTime;
		resp.getWriter().append(gson.toJson(order)).flush();
		resp.setStatus(201);
	}
	
}
