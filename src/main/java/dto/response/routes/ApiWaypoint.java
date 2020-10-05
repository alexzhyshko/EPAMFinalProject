package main.java.dto.response.routes;

import java.util.List;

public class ApiWaypoint {

	private String name;
	private List<Float> location;
	
	public ApiWaypoint(String name, List<Float> location) {
		this.name = name;
		this.location = location;
	}
}
