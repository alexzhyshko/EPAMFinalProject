package application.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ContextInitializer implements ServletContextListener {

	static Logger logger = Logger.getLogger("application");
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {
		String path = sce.getServletContext().getRealPath("/");
		try {
			logger.log(Level.INFO, "Path for configuration set to {0}", path);
			ApplicationContext.init(path);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		ApplicationContext.destroy();
	}

}
