package application.context.configuration;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigurationContext {

	private static List<Class> configurationClasses = new ArrayList<>();
	static Logger logger = Logger.getLogger("application");
	
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
		logger.log(Level.INFO, "Executed {0} config classes", configurationClasses.size());
	}

}
