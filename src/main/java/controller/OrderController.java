package main.java.controller;

import java.util.List;
import java.util.UUID;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.component.Component;
import application.context.annotation.component.RestController;
import application.context.annotation.inject.Inject;
import application.context.annotation.mapping.Mapping;
import application.context.annotation.mapping.RequestBody;
import application.context.annotation.mapping.RequestHeader;
import application.context.annotation.mapping.RequestParameter;
import application.context.annotation.mapping.RequestType;
import application.entity.ResponseEntity;
import application.utils.HttpUtils;
import main.java.dto.request.RouteCreateRequest;
import main.java.dto.response.RouteDetails;
import main.java.dto.response.UserOrdersResponse;
import main.java.entity.Order;
import main.java.exception.NoSuitableCarFound;
import main.java.service.LocalizationService;
import main.java.service.OrderService;

@Component
@RestController
public class OrderController {

	@Inject
	private OrderService orderService;

	@Inject
	private LocalizationService localizator;

	private static final String USER_LOCALE_HEADER_NAME="User_Locale";

	@Mapping(route = "/order/create:arg:arg", requestType = RequestType.POST)
	public ResponseEntity<Object> onOrderCreateRequestReceived(@RequestBody RouteCreateRequest requestObj,
			@RequestParameter("anyCategory") Boolean anyCategory,
			@RequestParameter("anyCategory") Boolean anyCountOfCars,
			@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		String jwt = HttpUtils.parseAuthHeader(userLocale);
		try {
			Order order = this.orderService.createOrder(userLocale, anyCategory, anyCountOfCars, jwt,
					requestObj);
			return new ResponseEntity<>(order, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_BAD_REQUEST, ContentType.TEXT_PLAIN);
		}
	}

	@Mapping(route = "/order/getRouteDetails", requestType = RequestType.POST)
	public ResponseEntity<Object> getRouteDetails(@RequestBody RouteCreateRequest requestObj,
			@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		try {
			List<RouteDetails> routeDetails = this.orderService.getRouteDetails(requestObj, userLocale)
					.orElseThrow(() -> new NoSuitableCarFound(
							localizator.getPropertyByLocale(userLocale, "couldNotFindMatchCarByPlaces")));
			return new ResponseEntity<>(routeDetails, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_NOT_FOUND, ContentType.TEXT_PLAIN);
		}
	}

	@Mapping(route = "/order/get/byUserId:arg:arg:arg", requestType = RequestType.GET)
	public ResponseEntity<Object> getOrdersByUserId(@RequestParameter("type") String type,
			@RequestParameter("userid") UUID userid,
			@RequestParameter("page") Integer page,
			@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		try {
			UserOrdersResponse response = this.orderService.getOrdersByUserId(userLocale, type, userid, page);
			return new ResponseEntity<>(response, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_NOT_FOUND, ContentType.TEXT_PLAIN);
		}
	}

	@Mapping(route = "/order/get/byId:arg", requestType = RequestType.GET)
	public ResponseEntity<Object> getOrderById(@RequestParameter("orderId") Integer orderId,
			@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		try {
			return new ResponseEntity<>(orderService.getOrderById(orderId, userLocale), HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (NullPointerException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_NOT_FOUND, ContentType.TEXT_PLAIN);
		}
		
	}

	@Mapping(route = "/order/finish:arg", requestType = RequestType.GET)
	public ResponseEntity<Object> onOrderFinishRequestREceived(@RequestParameter("orderId") Integer orderId,
			@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		if(orderService.finishOrder(orderId, userLocale)) 
			return new ResponseEntity<>(localizator.getPropertyByLocale(userLocale, "orderFinished"), HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		return new ResponseEntity<>(localizator.getPropertyByLocale(userLocale, "errorFinishingOrder"), HttpStatus.SC_INTERNAL_SERVER_ERROR, ContentType.TEXT_PLAIN);
	}

}
