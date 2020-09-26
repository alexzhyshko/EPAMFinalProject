package main.java.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

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
		try {
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
				resp.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
				resp.getWriter().append("Invalid page").flush();
				return;
			}
			int elementsPerPage = 15;
			List<Order> allOrders = new ArrayList<>();
			if (filter) {
				allOrders = orderService.getAllOrdersFiltered(userLocale, filterBy, value, page * elementsPerPage,
						page * elementsPerPage + elementsPerPage);
			} else {
				allOrders = orderService.getAllOrders(userLocale, page * elementsPerPage,
						page * elementsPerPage + elementsPerPage);
			}
			int totalNumberOfOrders = orderService.getTotalOrderCount(filterBy, value);
			if (sort) {
				Comparator<Order> comparator = null;
				if ("date".equals(sortBy)) {
					comparator = (order1, order2) -> order1.dateOfOrder.compareTo(order2.dateOfOrder);
				} else if ("price".equals(sortBy)) {
					comparator = (order1, order2) -> Float.compare(order1.price, order2.price);
				} else {
					resp.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
					resp.getWriter().append("Not supported sort option").flush();
					return;
				}

				allOrders.sort(sortOrder.equalsIgnoreCase("asc") ? comparator : comparator.reversed());
			}
//		if(filter) {
//			Predicate<Order> predicate = null;
//			if("user".equals(filterBy)) {
//				predicate = order1->order1.customer.getUsername().equalsIgnoreCase(value);
//			}else if("date".equals(filterBy)) {
//				DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
//				predicate = order1->order1.dateOfOrder.toLocalDate().isEqual(LocalDate.parse(value, df));
//			}else {
//				resp.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
//				resp.getWriter().append("Not supported filter option").flush();
//				return;
//			}
//			allOrders = allOrders.stream().filter(predicate).collect(Collectors.toList());
//		}

			UserOrdersResponse response = new UserOrdersResponse();
			response.numberOfPages = totalNumberOfOrders / elementsPerPage;
			if (totalNumberOfOrders % elementsPerPage != 0) {
				response.numberOfPages++;
			}
			response.orders = allOrders;
			if (totalNumberOfOrders == 0) {
				resp.setStatus(HttpStatus.SC_NOT_FOUND);
				resp.getWriter().append("Nothing found by your criteria").flush();
				return;
			}
			resp.setStatus(HttpStatus.SC_OK);
			resp.getWriter().append(gson.toJson(response)).flush();
		} catch (Exception e) {
			e.printStackTrace();
			resp.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
			resp.getWriter().append(e.getMessage()).flush();
		}
	}

}
