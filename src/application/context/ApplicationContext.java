package application.context;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import application.context.configuration.ConfigurationContext;
import application.context.inject.Injector;
import application.context.reader.AnnotationReader;
import application.context.rest.RestContext;
import application.context.scanner.Scanner;

public class ApplicationContext {

	private ApplicationContext() {
	}

	protected static HashMap<Class, Object> singletonComponents = new HashMap<>();
	protected static HashMap<Class, Object> prototypeComponents = new HashMap<>();

	static Logger logger = Logger.getLogger("application");
	
	protected static void init(String path) throws IOException {
		logger.info("Init started");
		Map<String, String> files = Scanner.getAllFilesInProject(path);
		AnnotationReader.process(files);
		ConfigurationContext.performConfiguration();
		Injector.inject();
		RestContext.performRestMapping();
		logger.info("Init finished");
	}

	protected static void destroy() {
		logger.info("Context destroyed");
	}

	public static Map<Class, Object> getSingletonComponents() {
		return singletonComponents;
	}

	public static Map<Class, Object> getPrototypeComponents() {
		return prototypeComponents;
	}

	public static void putIntoSingletonContext(Object object) {
		singletonComponents.put(object.getClass(), object);
	}

	public static Object getSingletonComponent(Class instanceClass) {
		return singletonComponents.get(instanceClass);
	}

	public static void putIntoPrototypeContext(Object object) {
		prototypeComponents.put(object.getClass(), object);
	}

	public static Object getPrototypeComponent(Class instanceClass)
			throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
		if (!prototypeComponents.containsKey(instanceClass)) {
			return null;
		}
		return instanceClass.getDeclaredConstructor().newInstance();
	}

	public static Object getInstance(Class clazz) {
		if (singletonComponents.get(clazz) != null) {
			return singletonComponents.get(clazz);
		}
		return prototypeComponents.get(clazz);
	}
	
	public static Object findSingletonByName(String name) {
		return getInstance(findSingletonClassByName(name));
	}
	
	private static Class findSingletonClassByName(String name) {
		return singletonComponents.keySet().stream().filter(fql->fql.getSimpleName().equals(name)).findFirst().orElseThrow(()->new NullPointerException("Class not found for name "+name));
	}

}
