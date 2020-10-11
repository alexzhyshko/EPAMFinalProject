package main.java.entity;

public class Route {

	private Coordinates departure;
	private Coordinates destination;
	private float distance;
	private int time;
	
	public static Builder builder() {
		return new Route().new Builder();
	}
	
	public class Builder{
		
		public Builder departure(Coordinates departure) {
			Route.this.departure = departure;
			return this;
		}
		
		public Builder destination(Coordinates destination) {
			Route.this.destination = destination;
			return this;
		}
		
		public Builder distance(float distance) {
			Route.this.distance = distance;
			return this;
		}
		
		public Builder time(int time) {
			Route.this.time = time;
			return this;
		}
		
		public Route build() {
			return Route.this;
		}
	}
	
	
	
	public Coordinates getDeparture() {
		return departure;
	}

	public Coordinates getDestination() {
		return destination;
	}

	public int getTime() {
		return time;
	}

	public float getDistance() {
		return this.distance;
	}
	
}
