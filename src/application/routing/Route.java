package application.routing;

import java.lang.reflect.Method;

public class Route {

	private String path;
	private Method method;
	private Class routeClass;

	public Route(String path, Method method, Class routeClass) {
		super();
		this.path = path;
		this.method = method;
		this.routeClass = routeClass;
	}

	public String getPath() {
		return path;
	}

	public Method getMethod() {
		return method;
	}

	public Class getRouteClass() {
		return routeClass;
	}
}
