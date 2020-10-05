package main.java.entity;

import java.time.LocalDateTime;

public class Order {

	private int id;
	private Route route;
	private float price;
	private User customer;
	private Car car;
	private Driver driver;
	private int timeToArrival;
	private LocalDateTime dateOfOrder;
	private String status;
	private int statusid;

	
	public static Builder builder() {
		return new Order().new Builder();
	}
	
	public class Builder {

		public Builder id(int id) {
			Order.this.id = id;
			return this;
		}

		public Builder route(Route route) {
			Order.this.route = route;
			return this;
		}

		public Builder price(float price) {
			Order.this.price = price;
			return this;
		}
		
		public Builder customer(User customer) {
			Order.this.customer = customer;
			return this;
		}

		public Builder car(Car car) {
			Order.this.car = car;
			return this;
		}
		
		public Builder driver(Driver driver) {
			Order.this.driver = driver;
			return this;
		}

		public Builder timeToArrival(int timeToArrival) {
			Order.this.timeToArrival = timeToArrival;
			return this;
		}

		public Builder dateOfOrder(LocalDateTime dateOfOrder) {
			Order.this.dateOfOrder = dateOfOrder;
			return this;
		}

		public Builder status(String status) {
			Order.this.status = status;
			return this;
		}

		public Builder statusid(int statusid) {
			Order.this.statusid = statusid;
			return this;
		}

		public Order build() {
			return Order.this;
		}

	}

	public int getId() {
		return id;
	}

	public Route getRoute() {
		return route;
	}

	public float getPrice() {
		return price;
	}

	public User getCustomer() {
		return customer;
	}

	public Car getCar() {
		return car;
	}

	public Driver getDriver() {
		return driver;
	}

	public int getTimeToArrival() {
		return timeToArrival;
	}

	public LocalDateTime getDateOfOrder() {
		return dateOfOrder;
	}

	public String getStatus() {
		return status;
	}

	public int getStatusid() {
		return statusid;
	}

	public void setTimeToArrival(int timeToArrival) {
		this.timeToArrival = timeToArrival;
	}
	
	

}
