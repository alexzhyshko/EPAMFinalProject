package main.java.entity;

public class Car {

	private int id;
	private String plate;
	private String manufacturer;
	private String model;
	private String category;
	private int passengerCount;
	private float priceMultiplier;
	private Status status;
	private Coordinates coordinates;
	
	
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
		
		public Builder category(String text) {
			Car.this.category = text;
			return this;
		}
		
		public Builder passengerCount(int count) {
			Car.this.passengerCount = count;
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
		
		public Builder coordinates(Coordinates coordinates) {
			Car.this.coordinates = coordinates;
			return this;
		}
		
		public Car build() {
			return Car.this;
		}
		
		
	}

	public int getId() {
		return id;
	}
	
	public String getPlate() {
		return plate;
	}

	public String getModel() {
		return model;
	}

	public String getManufacturer() {
		return manufacturer;
	}
	
	public String getCategory() {
		return category;
	}

	public int getPassengerCount() {
		return passengerCount;
	}

	public Status getStatus() {
		return status;
	}
	
	public Coordinates getCoordinates() {
		return coordinates;
	}
	
	public float getPriceMultiplier() {
		return priceMultiplier;
	}
	
	public void setStatus(Status status) {
		this.status = status;
	}
	
}
