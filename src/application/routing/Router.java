package application.routing;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import application.context.ApplicationContext;
import application.context.annotation.component.Component;
import application.context.annotation.mapping.RequestBody;
import application.context.annotation.mapping.RequestHeader;
import application.context.annotation.mapping.RequestParameter;
import application.entity.ResponseEntity;
import application.utils.HttpUtils;

@Component
public class Router {

	static Logger logger = Logger.getLogger("application");
	
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

	private Optional<Object> route(HttpServletRequest req, HttpServletResponse resp, HashMap<String, Route> routes) throws IOException {
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
				return Optional.empty();
			}
			Class destinationClass = destinationRoute.getRouteClass();
			Method destinationMethod = destinationRoute.getMethod();
			Object[] parameters = new Object[destinationMethod.getParameterCount()];
			Parameter[] methodParameters = destinationMethod.getParameters();
			for(int i = 0; i < methodParameters.length; i++) {
				Parameter methodParameter = methodParameters[i];
				Class parameterType = methodParameter.getType();
				if(parameterType == HttpServletRequest.class) {
					parameters[i] = req;
				}
				if(parameterType == HttpServletResponse.class) {
					parameters[i] = resp;
				}
				Annotation[] parameterAnnotations = methodParameter.getAnnotations();
				for(Annotation parameterAnnotation : parameterAnnotations) {
					if(parameterAnnotation instanceof RequestBody) {
						parameters[i] = HttpUtils.parseBody(req, parameterType).orElseThrow();
					}
					if(parameterAnnotation instanceof RequestHeader) {
						String headerName = ((RequestHeader)parameterAnnotation).value();
						parameters[i] = req.getHeader(headerName);
					}
					if(parameterAnnotation instanceof RequestParameter) {
						String parameterName = ((RequestParameter)parameterAnnotation).value();
						parameters[i] = HttpUtils.parseInputParameter(req, parameterName, parameterType);
					}
				}
			}
			return Optional.ofNullable(destinationMethod.invoke(ApplicationContext.getInstance(destinationClass), parameters));
		} catch ( SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | NullPointerException e) {
			logger.severe(e.getMessage());
			resp.setStatus(405);
			return Optional.empty();
		}
	}

	private Route getRouteByPath(HashMap<String, Route> routes, HttpServletRequest req, String path) {
		StringBuffer fullPath = new StringBuffer(path);
		if (req.getQueryString() != null) {
			for (String str : req.getParameterMap().keySet()) {
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

	public Optional<ResponseEntity<Object>> routeGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Object returnObject = route(req, resp, getRoutes);
		try {
			ResponseEntity<Object> responseEntity = ((Optional<ResponseEntity<Object>>)returnObject).get();
			return Optional.of(responseEntity);
		}catch(Exception e) {
			return Optional.empty();
		}
		
	}

	public Optional<ResponseEntity<Object>> routePost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Object returnObject = route(req, resp, postRoutes);
		try {
			ResponseEntity<Object> responseEntity = ((Optional<ResponseEntity<Object>>)returnObject).get();
			return Optional.ofNullable(responseEntity);
		}catch(Exception e) {
			logger.severe(e.getMessage());
			return Optional.empty();
		}
	}

	public Optional<ResponseEntity<Object>> routePut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Object returnObject = route(req, resp, putRoutes);
		try {
			ResponseEntity<Object> responseEntity = ((Optional<ResponseEntity<Object>>)returnObject).get();
			return Optional.ofNullable(responseEntity);
		}catch(Exception e) {
			logger.severe(e.getMessage());
			return Optional.empty();
		}
	}

	public Optional<ResponseEntity<Object>> routeDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		Object returnObject = route(req, resp, deleteRoutes);
		try {
			ResponseEntity<Object> responseEntity = ((Optional<ResponseEntity<Object>>)returnObject).get();
			return Optional.ofNullable(responseEntity);
		}catch(Exception e) {
			logger.severe(e.getMessage());
			return Optional.empty();
		}
	}

	/**
	 * Use this method to route GET requests on <i>path</i> to
	 * <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern:
	 * <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or
	 * <b>'/test:arg:arg'</b><br>
	 * 
	 * @param path       - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param method     - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router get(String path, Class routeClass, Method method) {
		this.getRoutes.put(path, new Route(path, method, routeClass));
		return this;
	}

	/**
	 * Use this method to route POST requests on <i>path</i> to
	 * <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern:
	 * <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or
	 * <b>'/test:arg:arg'</b><br>
	 * 
	 * @param path       - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param method     - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router post(String path, Class routeClass, Method method) {
		this.postRoutes.put(path, new Route(path, method, routeClass));
		return this;
	}

	/**
	 * Use this method to route PUT requests on <i>path</i> to
	 * <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern:
	 * <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or
	 * <b>'/test:arg:arg'</b><br>
	 * 
	 * @param path       - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param method     - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router put(String path, Class routeClass, Method method) {
		this.putRoutes.put(path, new Route(path, method, routeClass));
		return this;
	}

	/**
	 * Use this method to route DELETE requests on <i>path</i> to
	 * <i>routeClass#methodname</i><br>
	 * Path has to be relative to project root path.<br>
	 * Path should begin with '/'<br>
	 * If you need to path any path variables, use this pattern:
	 * <b>'/test/:pathVar'</b> or <b>'/test/:pathVar/test2/:pathVar'</b><br>
	 * If you need path arguments, use this pattern: <b>'/test:arg'</b> or
	 * <b>'/test:arg:arg'</b><br>
	 * 
	 * @param path       - path, which should be mapped to the routeClass
	 * @param routeClass - class, which should be used for processing mapped request
	 * @param method     - method in routeClass, which should process request
	 * @return Modified Router for cascading configuration
	 */
	public Router delete(String path, Class routeClass, Method method) {
		this.deleteRoutes.put(path, new Route(path, method, routeClass));
		return this;
	}
}
