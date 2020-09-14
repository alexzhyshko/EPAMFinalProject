package main.java;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import application.context.annotation.Component;
import application.context.annotation.Inject;
import application.context.annotation.Mapping;
import application.context.annotation.RestController;
import application.context.annotation.mapping.RequestType;


@Component
@RestController
public class Controller {

	@Inject
	private Service service;
	
	@Mapping(route="/", requestType=RequestType.GET)
	public void getDefaultRequest(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.getWriter().write(service.getUsername());
		resp.getWriter().flush();
		resp.getWriter().close();
	}
}
