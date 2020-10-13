package application.context.rest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.context.annotation.component.Component;
import application.context.annotation.inject.Inject;
import application.context.annotation.mapping.Mapping;
import application.context.annotation.mapping.RequestType;
import application.routing.Router;

@Component
public class RestContext {

	@Inject
	static Router router;
	
	private static List<Class> restClasses = new ArrayList<>();

	static Logger logger = Logger.getLogger("application");
	
	public static void addRest(Class restClass) {
		restClasses.add(restClass);
	}

	public static void performRestMapping() {
		for (Class rest : restClasses) {
			for(Method method : rest.getDeclaredMethods()) {
				Mapping annotation = method.getDeclaredAnnotation(Mapping.class);
				if(annotation!=null) {
					String route = annotation.route();
					RequestType type = annotation.requestType();	
					switch(type) {
					case GET:
						router.get(route, rest, method);
						break;
					case POST:
						router.post(route, rest, method);
						break;
					case PUT:
						router.put(route, rest, method);
						break;
					case DELETE:
						router.delete(route, rest, method);
						break;
					}
				}
			}
		}
		logger.log(Level.INFO, "Rest configuration finished, found {0} Rest Controllers", restClasses.size());
	}
	
	
}
