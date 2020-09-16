package main.java.dto;

public class Driver {

	private int id;
	private String name;
	private String surname;
	private float rating;
	
	public static Builder builder() {
		return new Driver().new Builder();
	}
	
	public class Builder{
		
		public Builder id(int id) {
			Driver.this.id = id;
			return this;
		}
		
		public Builder name(String text) {
			Driver.this.name = text;
			return this;
		}
		
		public Builder surname(String text) {
			Driver.this.surname = text;
			return this;
		}
		
		public Builder rating(float rating) {
			Driver.this.rating = rating;
			return this;
		}
		
		public Driver build() {
			return Driver.this;
		}
	}

	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}

	public String getSurname() {
		return surname;
	}

}
