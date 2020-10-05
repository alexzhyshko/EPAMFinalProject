package application.configurators;

import application.routing.Router;

public interface RouterConfiguratorAdapter extends ConfiguratorAdapter{

	public void configure(Router router);
	
}
