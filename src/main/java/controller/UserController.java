package main.java.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpStatus;

import com.google.gson.Gson;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;
import main.java.entity.User;
import main.java.service.UserService;

@Component
@RestController
public class UserController {

	@Inject
	private UserService userService;

	private Gson gson = new Gson();

	@Mapping(route = "/user/getById:arg", requestType = RequestType.GET)
	public void getUserById(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		UUID userId = null;
		try {
			userId = UUID.fromString(req.getParameter("userId"));
		} catch (Exception e) {
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append("Incorrect userId").flush();
			return;
		}
		try {
			User user = userService.getUserById(userId);
			resp.setContentType("text/json");
			resp.setStatus(HttpStatus.SC_OK);
			resp.getWriter().append(gson.toJson(user)).flush();
		} catch (NullPointerException e) {
			resp.setStatus(HttpStatus.SC_NOT_FOUND);
			resp.getWriter().append(e.getMessage()).flush();
		}
	}

	@Mapping(route = "/user/getByUsername:arg", requestType = RequestType.GET)
	public void getUserByUsername(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		String username = req.getParameter("username");
		if (username == null) {
			resp.setStatus(HttpStatus.SC_FORBIDDEN);
			resp.getWriter().append("Incorrect username").flush();
			return;
		}
		User user = userService.getUserByUsernameOrNull(username);
		if (user == null) {
			resp.setStatus(HttpStatus.SC_NOT_FOUND);
			resp.getWriter().append("User not found").flush();
			return;
		}
		resp.setContentType("text/json");
		resp.setStatus(HttpStatus.SC_OK);
		resp.getWriter().append(gson.toJson(user)).flush();
	}

}
