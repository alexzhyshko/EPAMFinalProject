package main.java.dto.response;

public class RouteDetails {
	private float price;
	private int arrivalTime;
	private String categoryLocaleName;
	
	public static Builder builder() {
		return new RouteDetails().new Builder();
	}
	
	public class Builder{
		
		public Builder price(float price) {
			RouteDetails.this.price = price;
			return this;
		}
		
		public Builder arrivalTime(int arrivalTime) {
			RouteDetails.this.arrivalTime = arrivalTime;
			return this;
		}
		
		public Builder categoryLocaleName(String categoryLocaleName) {
			RouteDetails.this.categoryLocaleName = categoryLocaleName;
			return this;
		}
		
		public RouteDetails build() {
			return RouteDetails.this;
		}
		
	}
	
}
