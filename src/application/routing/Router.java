package application.routing;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import application.context.ApplicationContext;
import application.context.annotation.Component;

@Component
public class Router {

	private HashMap<String, Route> getRoutes;
	private HashMap<String, Route> postRoutes;
	private HashMap<String, Route> putRoutes;
	private HashMap<String, Route> deleteRoutes;

	public Router() {
		this.getRoutes = new HashMap<>();
		this.postRoutes = new HashMap<>();
		this.putRoutes = new HashMap<>();
		this.deleteRoutes = new HashMap<>();
	}

	private void route(HttpServletRequest req, HttpServletResponse resp, HashMap<String, Route> routes) throws IOException {
		StringBuffer fullPath = req.getRequestURL();
		StringBuilder relativePath = new StringBuilder();
		if (fullPath.toString().split("/").length > 4) {
			String[] temp = fullPath.toString().split("/");
			for (int i = 4; i < temp.length; i++) {
				relativePath.append("/").append(temp[i]);
			}
		} else {
			relativePath.append("/");
		}
		try {
			Route destinationRoute = getRouteByPath(routes, req, relativePath.toString());
			if(destinationRoute==null) {
				resp.getWriter().append("Not found").flush();
				resp.setStatus(404);
				return;
			}
			Class destinationClass = destinationRoute.getRouteClass();
			Method destinationMethod = destinationClass.getMethod(destinationRoute.getMethodName(),
					HttpServletRequest.class, HttpServletResponse.class);
			destinationMethod.invoke(ApplicationContext.getInstance(destinationClass), req, resp);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | NullPointerException e) {
			e.printStackTrace();
			resp.setStatus(405);
		}
	}

	private Route getRouteByPath(HashMap<String, Route> routes, HttpServletRequest req, String path) {
		StringBuffer fullPath = new StringBuffer(path);
		if (req.getQueryString() != null) {
			for(String str : req.getParameterMap().keySet()) {
				fullPath.append(":arg");
			}
		}
		if (routes.get(fullPath.toString()) != null) {
			return routes.get(fullPath.toString());
		}
		String[] URLparts = Arrays.stream(path.split("/")).filter(e -> !e.trim().isEmpty()).collect(Collectors.toList())
				.toArray(new String[] {});
		List<String> filteredByStructure = routes.keySet().stream()
				.filter(e -> e.split("/").length == URLparts.length + 1).collect(Collectors.toList());
		String resultPath = "/";
		for (String routeKey : filteredByStructure) {
			String[] routeKeyParts = Arrays.stream(routeKey.split("/")).filter(e -> !e.trim().isEmpty())
					.collect(Collectors.toList()).toArray(new String[] {});
			for (int partIndex = 0; partIndex < URLparts.length; partIndex++) {
				if (URLparts[partIndex].equals(routeKeyParts[partIndex])) {
					resultPath = routeKey;
				} else {
					if (!":pathVar".equals(routeKeyParts[partIndex])) {
						resultPath = null;
						break;
					}
					resultPath = routeKey;
				}
			}
		}
		return routes.get(resultPath);

	}

	public void routeGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		route(req, resp, getRoutes);
	}

	public void routePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		route(req, resp, postRoutes);
	}

	public void routePut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		route(req, resp, putRoutes);
	}

	public void routeDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		route(req, resp, deleteRoutes);
	}

	
	/**
	 * Use this method to route GET requests on <i>path</i> to <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern: <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or <b>'/test:arg:arg'</b><br>
	 * @param path - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param methodName - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router get(String path, Class routeClass, String methodName) {
		this.getRoutes.put(path, new Route(path, methodName, routeClass));
		return this;
	}

	
	/**
	 * Use this method to route POST requests on <i>path</i> to <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern: <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or <b>'/test:arg:arg'</b><br>
	 * @param path - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param methodName - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router post(String path, Class routeClass, String methodName) {
		this.postRoutes.put(path, new Route(path, methodName, routeClass));
		return this;
	}

	
	/**
	 * Use this method to route PUT requests on <i>path</i> to <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern: <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or <b>'/test:arg:arg'</b><br>
	 * @param path - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param methodName - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router put(String path, Class routeClass, String methodName) {
		this.putRoutes.put(path, new Route(path, methodName, routeClass));
		return this;
	}

	
	/**
	 * Use this method to route DELETE requests on <i>path</i> to <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern: <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or <b>'/test:arg:arg'</b><br>
	 * @param path - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param methodName - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router delete(String path, Class routeClass, String methodName) {
		this.deleteRoutes.put(path, new Route(path, methodName, routeClass));
		return this;
	}
}
