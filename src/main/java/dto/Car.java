package main.java.dto;

public class Car {

	private int id;
	private String plate;
	private String manufacturer;
	private String model;
	private float priceMultiplier;
	private Status status;
	
	public static Builder builder() {
		return new Car().new Builder();
	}
	
	public class Builder{
		
		public Builder id(int id) {
			Car.this.id = id;
			return this;
		}
		
		public Builder plate(String text) {
			Car.this.plate = text;
			return this;
		}
		
		public Builder manufacturer(String text) {
			Car.this.manufacturer = text;
			return this;
		}
		
		public Builder model(String text) {
			Car.this.model = text;
			return this;
		}
		
		public Builder priceMultiplier(float number) {
			Car.this.priceMultiplier = number;
			return this;
		}
		
		public Builder status(Status status) {
			Car.this.status = status;
			return this;
		}
		
		public Car build() {
			return Car.this;
		}
		
		
	}

	public String getplate() {
		return plate;
	}

	public String getModel() {
		return model;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public Status getStatus() {
		return status;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
