package main.java.controller;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.dto.response.UserOrdersResponse;
import main.java.entity.Order;
import main.java.service.OrderService;

@Component
@RestController
public class AdminController {

	
	@Inject
	private OrderService orderService;
	
	private Gson gson = new Gson();
	
	@Mapping(route = "/admin/order/get/all:arg:arg:arg:arg:arg:arg:arg", requestType = RequestType.GET)
	public void getAllOrdersSortedFiltered(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String userLocale = req.getHeader("User_Locale");
		boolean sort = req.getParameter("sort").equalsIgnoreCase("true");
		boolean filter = req.getParameter("filter").equalsIgnoreCase("true");
		String sortBy = req.getParameter("sortBy");
		String sortOrder = req.getParameter("sortOrder");
		String filterBy = req.getParameter("filterBy");
		String value = req.getParameter("value");
		int page = 0;
		try {
			page = Integer.parseInt(req.getParameter("page"));
		} catch (Exception e) {
			e.printStackTrace();
			//TODO change dispatcher
		}
		List<Order> allOrders = orderService.getAllOrders(userLocale);
		if(sort) {
			Comparator<Order> comparator = null;
			if("date".equals(sortBy)) {
				comparator = (order1, order2)->order1.dateOfOrder.compareTo(order2.dateOfOrder);
			}else if("price".equals(sortBy)) {
				comparator = (order1, order2)->Float.compare(order1.price, order2.price);
			}else {
				resp.getWriter().append("Not supported sort option").flush();
				resp.setStatus(403);
				return;
			}
			
			allOrders.sort(sortOrder.equalsIgnoreCase("asc")?comparator:comparator.reversed());
		}
		if(filter) {
			Predicate<Order> predicate = null;
			if("user".equals(filterBy)) {
				predicate = order1->order1.customer.getUsername().equalsIgnoreCase(value);
			}else if("date".equals(filterBy)) {
				DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				predicate = order1->order1.dateOfOrder.toLocalDate().isEqual(LocalDate.parse(value, df));
			}else {
				resp.getWriter().append("Not supported filter option").flush();
				resp.setStatus(403);
				return;
			}
			allOrders = allOrders.stream().filter(predicate).collect(Collectors.toList());
		}
		int elementsPerPage = 4;
		UserOrdersResponse response = new UserOrdersResponse();
		response.numberOfPages = allOrders.size() / elementsPerPage;
		if (allOrders.size() % elementsPerPage != 0) {
			response.numberOfPages++;
		}
		response.orders = allOrders.stream().limit(page * elementsPerPage + elementsPerPage).skip(page * elementsPerPage)
				.collect(Collectors.toList());
		resp.getWriter().append(gson.toJson(response)).flush();
		resp.setStatus(200);
	}
	
}
