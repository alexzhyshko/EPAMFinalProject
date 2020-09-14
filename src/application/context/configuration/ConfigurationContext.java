package application.context.configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ConfigurationContext {

	private static List<Class> configurationClasses = new ArrayList<>();

	public static void addConfig(Class configClass) {
		configurationClasses.add(configClass);
	}

	public static void performConfiguration() {
		for (Class config : configurationClasses) {
			try {
				ConfigurationInvoker.invoke(config);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

}
