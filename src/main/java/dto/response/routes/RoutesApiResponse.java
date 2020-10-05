package main.java.dto.response.routes;

import java.util.List;

public class RoutesApiResponse {

	private List<ApiRoute> routes;
	private List<ApiWaypoint> waypoints;
	
	public RoutesApiResponse(List<ApiRoute> routes, List<ApiWaypoint> waypoints) {
		this.routes = routes;
		this.waypoints = waypoints;
	}

	public List<ApiRoute> getRoutes() {
		return routes;
	}
	
	
}
