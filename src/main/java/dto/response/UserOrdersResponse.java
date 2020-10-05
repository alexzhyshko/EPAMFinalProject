package main.java.dto.response;

import java.util.List;

import main.java.entity.Order;

public class UserOrdersResponse {

	private List<Order> orders;
	private int numberOfPages;
	
	public static Builder builder() {
		return new UserOrdersResponse().new Builder();
	}
	
	public class Builder{
		
		public Builder orders(List<Order> orders) {
			UserOrdersResponse.this.orders = orders;
			return this;
		}
		
		public Builder numberOfPages(int numberOfPages) {
			UserOrdersResponse.this.numberOfPages = numberOfPages;
			return this;
		}
		
		public UserOrdersResponse build() {
			return UserOrdersResponse.this;
		}
		
	}
	
}
