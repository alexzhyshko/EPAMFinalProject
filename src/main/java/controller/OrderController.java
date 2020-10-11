package main.java.controller;

import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.dto.request.RouteCreateRequest;
import main.java.dto.response.RouteDetails;
import main.java.dto.response.UserOrdersResponse;
import main.java.entity.Order;
import main.java.exception.CouldNotParseBodyException;
import main.java.exception.NoSuitableCarFound;
import main.java.service.LocalizationService;
import main.java.service.OrderService;
import main.java.utils.HttpUtils;

@Component
@RestController
public class OrderController {

	@Inject
	private OrderService orderService;

	@Inject
	private LocalizationService localizator;

	

	@Mapping(route = "/order/create:arg:arg", requestType = RequestType.POST)
	public void onOrderCreateRequestReceived(HttpServletRequest req, HttpServletResponse resp){
		String userLocale = req.getHeader("User_Locale");
		String jwt = HttpUtils.parseAuthHeader(req);
		try {
			boolean anyCategory = HttpUtils.parseInputParameter(req, "anyCategory", userLocale, Boolean.class);
			boolean anyCountOfCars = HttpUtils.parseInputParameter(req, "anyCountOfCars", userLocale, Boolean.class);
			RouteCreateRequest requestObj = HttpUtils.parseBody(req, RouteCreateRequest.class)
					.orElseThrow(() -> new CouldNotParseBodyException("Could not parse body"));
			Order order = this.orderService.createOrder(userLocale, anyCategory, anyCountOfCars, jwt,
					requestObj);
			HttpUtils.setResponseBody(resp, order, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
		} catch (Exception e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_BAD_REQUEST);
		}
	}

	@Mapping(route = "/order/getRouteDetails", requestType = RequestType.POST)
	public void getRouteDetails(HttpServletRequest req, HttpServletResponse resp){
		String userLocale = req.getHeader("User_Locale");
		try {
			RouteCreateRequest requestObj = HttpUtils.parseBody(req, RouteCreateRequest.class)
					.orElseThrow(() -> new CouldNotParseBodyException("Could not parse body"));
			List<RouteDetails> routeDetails = this.orderService.getRouteDetails(requestObj, userLocale)
					.orElseThrow(() -> new NoSuitableCarFound(
							localizator.getPropertyByLocale(userLocale, "couldNotFindMatchCarByPlaces")));
			HttpUtils.setResponseBody(resp, routeDetails, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_NOT_FOUND);
		}
	}

	@Mapping(route = "/order/get/byUserId:arg:arg:arg", requestType = RequestType.GET)
	public void getOrdersByUserId(HttpServletRequest req, HttpServletResponse resp){
		String userLocale = req.getHeader("User_Locale");
		String type = HttpUtils.parseInputParameter(req, "type", userLocale, String.class);
		UUID userid = HttpUtils.parseInputParameter(req, "userId", userLocale, UUID.class);
		int page = HttpUtils.parseInputParameter(req, "page", userLocale, Integer.class);
		try {
			UserOrdersResponse response = this.orderService.getOrdersByUserId(userLocale, type, userid, page);
			HttpUtils.setResponseBody(resp, response, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
		} catch (Exception e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_NOT_FOUND);
		}
	}

	@Mapping(route = "/order/get/byId:arg", requestType = RequestType.GET)
	public void getOrderById(HttpServletRequest req, HttpServletResponse resp){
		String userLocale = req.getHeader("User_Locale");
		try {
			int orderId = HttpUtils.parseInputParameter(req, "orderId", userLocale, Integer.class);
			HttpUtils.setResponseBody(resp, orderService.getOrderById(orderId, userLocale), ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
		} catch (NullPointerException e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_NOT_FOUND);
		} catch (IllegalArgumentException e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_BAD_REQUEST);
		}
		
	}

	@Mapping(route = "/order/finish:arg", requestType = RequestType.GET)
	public void onOrderFinishRequestREceived(HttpServletRequest req, HttpServletResponse resp){
		String userLocale = req.getHeader("User_Locale");
		int orderId = HttpUtils.parseInputParameter(req, "orderId", userLocale, Integer.class);
		if(orderService.finishOrder(orderId, userLocale)) 
			HttpUtils.setResponseBody(resp, localizator.getPropertyByLocale(userLocale, "orderFinished"), ContentType.TEXT_PLAIN, HttpStatus.SC_OK);
		HttpUtils.setResponseBody(resp, localizator.getPropertyByLocale(userLocale, "errorFinishingOrder"), ContentType.TEXT_PLAIN, HttpStatus.SC_INTERNAL_SERVER_ERROR);
	}

}
