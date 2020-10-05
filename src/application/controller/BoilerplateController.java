package application.controller;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.HttpConstraint;
import javax.servlet.annotation.ServletSecurity;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import application.context.ApplicationContext;
import application.routing.Router;

public class BoilerplateController extends HttpServlet {

	private static Router router = (Router) ApplicationContext.getSingletonComponent(Router.class);

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			router.routeGet(req, resp);
		} catch (IOException e) {
			// TODO implement logging
			e.printStackTrace();
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			router.routePost(req, resp);
		} catch (IOException e) {
			// TODO implement logging
			e.printStackTrace();
		}
	}

	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			router.routePut(req, resp);
		} catch (IOException e) {
			// TODO implement logging
			e.printStackTrace();
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
		try {
			router.routeDelete(req, resp);
		} catch (IOException e) {
			// TODO implement logging
			e.printStackTrace();
		}
	}

}
