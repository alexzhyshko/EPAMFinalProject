package main.java.controller;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import application.context.annotation.Component;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;

@Component
@RestController
public class AdminResource {

	
	@Mapping(route = "/admin/resource", requestType = RequestType.GET)
	public void getLoginRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException{
		resp.getWriter().append("Admin resource").flush();
		resp.setStatus(200);
	}
	
}
