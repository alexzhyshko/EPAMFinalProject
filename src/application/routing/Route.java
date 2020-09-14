package application.routing;

public class Route {

	private String path;
	private String methodName;
	private Class routeClass;

	public Route(String path, String methodName, Class routeClass) {
		super();
		this.path = path;
		this.methodName = methodName;
		this.routeClass = routeClass;
	}

	public String getPath() {
		return path;
	}

	public String getMethodName() {
		return methodName;
	}

	public Class getRouteClass() {
		return routeClass;
	}
}
