package application.context.reader;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Map.Entry;

import application.context.ApplicationContext;
import application.context.annotation.Component;
import application.context.annotation.Configuration;
import application.context.annotation.Prototype;
import application.context.annotation.RestController;
import application.context.configuration.ConfigurationContext;
import application.context.rest.RestContext;

public class AnnotationReader {

	public static void process(Map<String, String> files) {
		try {
			for (Entry<String, String> entry : files.entrySet()) {
				Class temp = Class.forName(entry.getValue());
				if (hasComponentAnnotation(temp)) {
					if (!hasPrototypeAnnotation(temp)) {
						ApplicationContext.putIntoSingletonContext(getInstanceOfClass(temp));
					}
					else
						ApplicationContext.putIntoPrototypeContext(getInstanceOfClass(temp));
					if(hasRestControllerAnnotation(temp)) {
						RestContext.addRest(temp);
					}
				} else if (hasConfigurationAnnotation(temp))
					ConfigurationContext.addConfig(temp);

			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
	}

	protected static boolean hasRestControllerAnnotation(Class clazz) {
		return clazz.getDeclaredAnnotation(RestController.class) != null;
	}

	
	protected static boolean hasComponentAnnotation(Class clazz) {
		return clazz.getDeclaredAnnotation(Component.class) != null;
	}

	protected static boolean hasConfigurationAnnotation(Class clazz) {
		return clazz.getDeclaredAnnotation(Configuration.class) != null;
	}

	protected static boolean hasPrototypeAnnotation(Class clazz) {
		return clazz.getDeclaredAnnotation(Prototype.class) != null;
	}

	
	protected static Object getInstanceOfClass(Class clazz) throws InstantiationException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		return clazz.getDeclaredConstructor().newInstance();
	}

}
