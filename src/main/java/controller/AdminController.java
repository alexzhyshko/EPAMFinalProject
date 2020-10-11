package main.java.controller;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.component.Component;
import application.context.annotation.component.RestController;
import application.context.annotation.inject.Inject;
import application.context.annotation.mapping.Mapping;
import application.context.annotation.mapping.RequestHeader;
import application.context.annotation.mapping.RequestParameter;
import application.context.annotation.mapping.RequestType;
import application.entity.ResponseEntity;
import main.java.dto.PaginationFilteringSortingDTO;
import main.java.dto.response.UserOrdersResponse;
import main.java.exception.NothingFoundException;
import main.java.service.LocalizationService;
import main.java.service.OrderService;

@Component
@RestController
public class AdminController {

	@Inject
	private OrderService orderService;

	@Inject
	LocalizationService localizator;
	
	@Mapping(route = "/admin/order/get/all:arg:arg:arg:arg:arg:arg:arg", requestType = RequestType.GET)
	public ResponseEntity<Object> getAllOrdersSortedFiltered(@RequestParameter("sort") Boolean sort,
			@RequestParameter("filter") Boolean filter,
			@RequestParameter("sortBy") String sortBy,
			@RequestParameter("sortOrder") String sortOrder,
			@RequestParameter("filterBy") String filterBy,
			@RequestParameter("value") String value,
			@RequestHeader("User_Locale") String userLocale){
		PaginationFilteringSortingDTO dto = PaginationFilteringSortingDTO.builder()
				.sort(sort)
				.filter(filter)
				.sortBy(sortBy)
				.order(sortOrder)
				.filterBy(filterBy)
				.value(value)
				.build();
		try {
			UserOrdersResponse response = this.orderService.getAllOrdersSortedFiltered(dto, userLocale).orElseThrow(()->new NothingFoundException("Nothing found by your criteria"));
			return new ResponseEntity<>(response, HttpStatus.SC_OK, ContentType.APPLICATION_JSON);
		} catch (NothingFoundException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.SC_NOT_FOUND, ContentType.TEXT_PLAIN);
		}
	}

}
