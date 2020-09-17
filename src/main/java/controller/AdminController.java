package main.java.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.service.OrderService;

@Component
@RestController
public class AdminController {

	
	@Inject
	private OrderService orderService;
	
	private Gson gson = new Gson();
	
	@Mapping(route = "/admin/getAllOrdersByUsers", requestType = RequestType.GET)
	public void getAllOrdersByUsers(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		String userLocale = req.getHeader("User_Locale");
		if (userLocale == null) {
			resp.getWriter().append("User locale not set").flush();
			resp.setStatus(403);
			return;
		}
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(orderService.getAllOrders(userLocale))).flush();
		resp.setStatus(200);
	}
	
}
