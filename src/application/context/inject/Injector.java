package application.context.inject;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map.Entry;

import application.context.ApplicationContext;
import application.context.annotation.Inject;

public class Injector {

	public static void inject() {
		HashMap<Class, Object> allComponents = new HashMap<>();
		try {
			allComponents.putAll(ApplicationContext.getSingletonComponents());
			allComponents.putAll(ApplicationContext.getPrototypeComponents());
			for (Entry<Class, Object> entry : allComponents.entrySet()) {
				Class clazz = entry.getKey();
				Field[] fields = clazz.getDeclaredFields();
				for (Field field : fields) {
					if (field.isAnnotationPresent(Inject.class)) {
						Object injectingObject = ApplicationContext.getSingletonComponent(field.getType());
						if (injectingObject == null) {
							injectingObject = ApplicationContext.getPrototypeComponent(field.getType());
						}
						if (injectingObject == null) {
							throw new NullPointerException("Component for type " + field.getType()
									+ " not found in Application Context. Couldn't inject into " + clazz.getName());
						}
						field.setAccessible(true);
						field.set(entry.getValue(), injectingObject);
						field.setAccessible(false);
					}
				}
			}
		} catch (IllegalArgumentException | IllegalAccessException | InstantiationException | InvocationTargetException
				| NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

}
