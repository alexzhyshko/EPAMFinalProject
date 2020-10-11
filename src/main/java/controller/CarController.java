package main.java.controller;

import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.component.Component;
import application.context.annotation.component.RestController;
import application.context.annotation.inject.Inject;
import application.context.annotation.mapping.Mapping;
import application.context.annotation.mapping.RequestHeader;
import application.context.annotation.mapping.RequestType;
import application.entity.ResponseEntity;
import main.java.entity.Car;
import main.java.service.CarService;

@Component
@RestController
public class CarController {

	private static final String USER_LOCALE_HEADER_NAME="User_Locale";
	
	@Inject
	CarService carService;
	
	@Mapping(route = "/car/getAll", requestType = RequestType.GET)
	public ResponseEntity<Object> getAllCars(@RequestHeader(USER_LOCALE_HEADER_NAME) String userLocale){
		List<Car> allCars = carService.getAllAvailableCars(userLocale);
		return new ResponseEntity<>(allCars, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
	}
	
}
