package main.java.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;
import org.apache.http.entity.ContentType;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.dto.PaginationFilteringSortingDTO;
import main.java.dto.response.UserOrdersResponse;
import main.java.exception.NothingFoundException;
import main.java.service.LocalizationService;
import main.java.service.OrderService;
import main.java.utils.HttpUtils;

@Component
@RestController
public class AdminController {

	@Inject
	private OrderService orderService;

	@Inject
	LocalizationService localizator;
	
	private PaginationFilteringSortingDTO parseDtoFromRequest(HttpServletRequest req, String userLocale) {
		boolean sort = req.getParameter("sort").equalsIgnoreCase("true");
		boolean filter = req.getParameter("filter").equalsIgnoreCase("true");
		String sortBy = req.getParameter("sortBy");
		String sortOrder = req.getParameter("sortOrder");
		String filterBy = req.getParameter("filterBy");
		String value = req.getParameter("value");
		int page = HttpUtils.parseInputParameter(req, "page", userLocale, Integer.class);
		return PaginationFilteringSortingDTO.builder()
			.sort(sort)
			.filter(filter)
			.sortBy(sortBy)
			.order(sortOrder)
			.filterBy(filterBy)
			.value(value)
			.page(page)
			.build();
	}
	
	@Mapping(route = "/admin/order/get/all:arg:arg:arg:arg:arg:arg:arg", requestType = RequestType.GET)
	public void getAllOrdersSortedFiltered(HttpServletRequest req, HttpServletResponse resp){
		String userLocale = req.getHeader("User_Locale");
		PaginationFilteringSortingDTO dto = parseDtoFromRequest(req, userLocale);
		try {
			UserOrdersResponse response = this.orderService.getAllOrdersSortedFiltered(dto, userLocale).orElseThrow(()->new NothingFoundException("Nothing found by your criteria"));
			HttpUtils.setResponseBody(resp, response, ContentType.APPLICATION_JSON, HttpStatus.SC_OK);
		} catch (Exception e) {
			HttpUtils.setResponseBody(resp, e.getMessage(), ContentType.TEXT_PLAIN, HttpStatus.SC_OK);
		}
	}

}
