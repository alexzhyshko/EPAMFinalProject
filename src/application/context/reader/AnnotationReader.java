package application.context.reader;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.context.ApplicationContext;
import application.context.annotation.component.Component;
import application.context.annotation.component.Prototype;
import application.context.annotation.component.RestController;
import application.context.annotation.configuration.Configuration;
import application.context.cast.Caster;
import application.context.cast.CasterContext;
import application.context.configuration.ConfigurationContext;
import application.context.rest.RestContext;

public class AnnotationReader {

	static Logger logger = Logger.getLogger("application");
	
	public static void process(Map<String, String> files) {
		try {
			for (Entry<String, String> entry : files.entrySet()) {
				Class temp = Class.forName(entry.getValue());
				if (hasComponentAnnotation(temp)) {
					if (!hasPrototypeAnnotation(temp)) {
						ApplicationContext.putIntoSingletonContext(getInstanceOfClass(temp));
					} else
						ApplicationContext.putIntoPrototypeContext(getInstanceOfClass(temp));
					if (hasRestControllerAnnotation(temp)) {
						RestContext.addRest(temp);
					}
					if(Arrays.asList(temp.getGenericInterfaces()).contains(Caster.class)){
						CasterContext.addCaster(temp);
					}
				} else if (hasConfigurationAnnotation(temp))
					ConfigurationContext.addConfig(temp);

			}
		} catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException | InstantiationException e) {
			e.printStackTrace();
		}
		logger.log(Level.INFO, "Processed {0} classes", files.size());
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

	protected static Object getInstanceOfClass(Class clazz)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		return clazz.getDeclaredConstructor().newInstance();
	}

}
