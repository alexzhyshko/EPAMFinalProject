package main.java.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.dto.User;
import main.java.service.UserService;

@Component
@RestController
public class UserController {

	@Inject
	private UserService userService;

	private Gson gson;

	@Mapping(route = "/user/getById:arg", requestType = RequestType.GET)
	public void getUserById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UUID userId = null;
		try {
			userId = UUID.fromString(req.getParameter("userId"));
		} catch (Exception e) {
			resp.getWriter().append("Incorrect userId").flush();
			resp.setStatus(403);
			return;
		}
		User user = userService.getUserById(userId);
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(user)).flush();
		resp.setStatus(200);
	}

	@Mapping(route = "/user/getByUsername:arg", requestType = RequestType.GET)
	public void getUserByUsername(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String username = req.getParameter("username");
		if (username == null) {
			resp.getWriter().append("Incorrect username").flush();
			resp.setStatus(403);
			return;
		}
		User user = userService.getUserByUsernameOrNull(username);
		if(user == null) {
			resp.getWriter().append("User not found").flush();
			resp.setStatus(404);
			return;
		}
		resp.setContentType("text/json");
		resp.getWriter().append(gson.toJson(user)).flush();
		resp.setStatus(200);
	}

}
