package main.java.controller;

import java.io.IOException;
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
import main.java.service.LocalizationService;
import main.java.service.OrderService;

@Component
@RestController
public class AdminController {

	@Inject
	private OrderService orderService;

	@Inject
	LocalizationService localizator;
	
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
				resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "invalidPage")).flush();
				return;
			}
			int elementsPerPage = 15;
			List<Order> allOrders;
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
				if ("dateOfOrder".equals(sortBy)) {
					comparator = (order1, order2) -> order1.getDateOfOrder().compareTo(order2.getDateOfOrder());
				} else if ("price".equals(sortBy)) {
					comparator = (order1, order2) -> Float.compare(order1.getPrice(), order2.getPrice());
				} else {
					resp.setStatus(HttpStatus.SC_NOT_ACCEPTABLE);
					resp.getWriter().append(localizator.getPropertyByLocale(userLocale, "notSupportedSortOption")).flush();
					return;
				}

				allOrders.sort(sortOrder.equalsIgnoreCase("asc") ? comparator : comparator.reversed());
			}
			int numberOfPages = totalNumberOfOrders / elementsPerPage;
			if (totalNumberOfOrders % elementsPerPage != 0) {
				numberOfPages++;
			}
			UserOrdersResponse response = UserOrdersResponse.builder()
					.numberOfPages(numberOfPages)
					.orders(allOrders)
					.build();
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
